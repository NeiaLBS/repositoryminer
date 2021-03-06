package org.repositoryminer.metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.repositoryminer.RepositoryMinerException;
import org.repositoryminer.domain.Commit;
import org.repositoryminer.metrics.codemetric.CodeMetric;
import org.repositoryminer.metrics.codesmell.CodeSmell;
import org.repositoryminer.metrics.parser.Parser;
import org.repositoryminer.metrics.persistence.CodeAnalysisConfigDAO;
import org.repositoryminer.plugin.SnapshotAnalysisPlugin;

public class RMMetrics extends SnapshotAnalysisPlugin<MetricsConfig> {

	@Override
	public boolean run(String snapshot, MetricsConfig config) {
		if (!config.isValid()) {
			throw new RepositoryMinerException(
					"Invalid configuration, check if has parser and code metrics or codes mells.");
		}

		scm.checkout(snapshot);

		AnalysisRunner runner = new AnalysisRunner();
		runner.setCodeMetrics(config.getCodeMetrics());
		runner.setCodeSmells(config.getCodeSmells());
		runner.setParsers(config.getParsers());

		ObjectId configId = persistAnalysisConfig(config.getParsers(), runner.getCalculatedMetrics(),
				runner.getDetectedCodeSmells());
		try {
			runner.run(tmpRepository, configId);
		} catch (IOException e) {
			throw new RepositoryMinerException(e);
		}

		return true;
	}

	private ObjectId persistAnalysisConfig(List<Parser> usedParsers, Collection<CodeMetric> calculatedMetrics,
			Collection<CodeSmell> detectedCodeSmells) {
		CodeAnalysisConfigDAO configDao = new CodeAnalysisConfigDAO();
		Commit commit = scm.getHEAD();

		List<String> metricsNames = new ArrayList<>();
		for (CodeMetric cm : calculatedMetrics) {
			metricsNames.add(cm.getId().name());
		}

		List<String> parsersNames = new ArrayList<>();
		for (Parser p : usedParsers) {
			parsersNames.add(p.getId().name());
		}

		List<Document> codeSmellsDoc = new ArrayList<>();
		for (CodeSmell codeSmell : detectedCodeSmells) {
			codeSmellsDoc.add(new Document("codesmell", codeSmell.getId().name()).append("thresholds",
					codeSmell.getThresholds()));
		}

		Document doc = new Document("commit", commit.getHash()).append("commit_date", commit.getCommitterDate())
				.append("analysis_date", new Date(System.currentTimeMillis())).append("repository", repositoryId)
				.append("parsers", parsersNames).append("metrics", metricsNames).append("codesmells", codeSmellsDoc);

		configDao.insert(doc);

		return doc.getObjectId("_id");
	}

}