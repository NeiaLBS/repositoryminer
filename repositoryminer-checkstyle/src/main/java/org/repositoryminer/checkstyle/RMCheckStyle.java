package org.repositoryminer.checkstyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.repositoryminer.RepositoryMinerException;
import org.repositoryminer.checkstyle.model.StyleProblem;
import org.repositoryminer.checkstyle.persistence.CheckstyleAuditDAO;
import org.repositoryminer.domain.Commit;
import org.repositoryminer.plugin.SnapshotAnalysisPlugin;
import org.repositoryminer.util.RMFileUtils;
import org.repositoryminer.util.StringUtils;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class RMCheckStyle extends SnapshotAnalysisPlugin<CheckStyleConfig> {

	@Override
	public boolean run(String snapshot, CheckStyleConfig config) {
		scm.checkout(snapshot);
		CheckStyleExecutor executor = new CheckStyleExecutor(tmpRepository);
		
		if (config != null) {
			if (config.isInsideRepository()) {
				executor.setConfigFile(config.getConfigFile() != null ?
						RMFileUtils.concatFilePath(tmpRepository, config.getConfigFile()) : null);
				executor.setPropertiesFile(config.getPropertiesFile() != null ?
						RMFileUtils.concatFilePath(tmpRepository, config.getPropertiesFile()) : null);
			} else {
				executor.setConfigFile(config.getConfigFile());
				executor.setPropertiesFile(config.getPropertiesFile());
			}
		}
		

		Map<String, List<StyleProblem>> result = null;
		try {
			result = executor.execute();
		} catch (CheckstyleException e) {
			throw new RepositoryMinerException("Can not execute checkstyle.", e);
		}

		Commit commit = scm.getHEAD();

		List<Document> documents = new ArrayList<Document>(result.size());
		for (Entry<String, List<StyleProblem>> file : result.entrySet()) {
			Document doc = new Document("commit", commit.getId()).
					append("filehash", StringUtils.encodeToCRC32(file.getKey())).
					append("commit_date", commit.getCommitterDate()).
					append("repository", repositoryId).
					append("filename", file.getKey()).
					append("style_problems", StyleProblem.toDocumentList(file.getValue()));

			documents.add(doc);
		}

		new CheckstyleAuditDAO().insertMany(documents);
		return true;
	}

}
