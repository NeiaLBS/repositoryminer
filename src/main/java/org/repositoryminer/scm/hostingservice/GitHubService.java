package org.repositoryminer.scm.hostingservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.repositoryminer.mining.HostingServiceMiner;
import org.repositoryminer.model.Comment;
import org.repositoryminer.model.Contributor;
import org.repositoryminer.model.Issue;
import org.repositoryminer.model.Label;
import org.repositoryminer.model.Milestone;

public class GitHubService implements IHostingService {

	private IssueService issueServ;
	private MilestoneService milestoneServ;
	private RepositoryId repositoryId;
	private CollaboratorService collaboratorServ;
	private RepositoryService repoServ;
	private HostingServiceMiner hostingMiner;
	
	// Initializes repository and needed services.
	private void init(HostingServiceMiner hostingMiner, GitHubClient client) {
		this.repositoryId = new RepositoryId(hostingMiner.getOwner(), hostingMiner.getName());
		this.issueServ = new IssueService(client);
		this.milestoneServ = new MilestoneService(client);
		this.collaboratorServ = new CollaboratorService(client);
		this.repoServ = new RepositoryService(client);
		this.hostingMiner = hostingMiner;
	}

	@Override
	public void connect(HostingServiceMiner hostingMiner, String login, String password) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(login, password);
		init(hostingMiner, client);
	}

	@Override
	public void connect(HostingServiceMiner hostingMiner, String token) {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(token);
		init(hostingMiner, client);
	}

	@Override
	public List<Issue> getAllIssues() {
		if (hostingMiner.getListener() != null) {
			hostingMiner.getListener().initIssuesProcessing();
		}

		int number = 1;
		List<Issue> issues = new ArrayList<Issue>();

		while (true) {
			try {
				org.eclipse.egit.github.core.Issue izzue = issueServ.getIssue(repositoryId, number);
				Issue issue = new Issue(izzue.getUser().getLogin(), izzue.getClosedAt(), izzue.getCreatedAt(),
						izzue.getNumber(), StatusType.parse(izzue.getState()), izzue.getTitle(), izzue.getUpdatedAt(),
						izzue.getBody());

				if (izzue.getAssignee() != null) {
					issue.setAssignee(izzue.getAssignee().getLogin());
				}

				if (izzue.getMilestone() != null) {
					issue.setMilestone(izzue.getMilestone().getNumber());
				}

				if (izzue.getLabels() != null) {
					List<Label> labels = new ArrayList<Label>();
					for (org.eclipse.egit.github.core.Label l : izzue.getLabels()) {
						labels.add(new Label(l.getName(), l.getColor()));
					}
					issue.setLabels(labels);
				}

				List<org.eclipse.egit.github.core.Comment> commentz = issueServ.getComments(repositoryId, number);

				if (commentz != null) {
					List<Comment> comments = new ArrayList<Comment>();

					for (org.eclipse.egit.github.core.Comment c : commentz) {
						Comment comment = new Comment(c.getUser().getLogin(), c.getBody(), c.getCreatedAt(),
								c.getUpdatedAt());
						comments.add(comment);
					}

					issue.setComments(comments);
				}
				
				number++;
				issues.add(issue);
			} catch (IOException e) {
				break;
			}
		}

		return issues;
	}

	@Override
	public List<Milestone> getAllMilestones() {
		if (hostingMiner.getListener() != null) {
			hostingMiner.getListener().initMilestonesProcessing();
		}

		int number = 1;
		List<Milestone> milesDB = new ArrayList<Milestone>();
		while (true) {
			try {
				org.eclipse.egit.github.core.Milestone mile = milestoneServ.getMilestone(repositoryId, number);
				Milestone mileDB = new Milestone(mile.getNumber(), StatusType.parse(mile.getState()), mile.getTitle(),
						mile.getDescription(), mile.getOpenIssues(), mile.getClosedIssues(), mile.getCreatedAt(),
						mile.getDueOn());

				if (mile.getCreator() != null) {
					mileDB.setCreator(mile.getCreator().getLogin());
				}

				number++;
				milesDB.add(mileDB);
			} catch (IOException e) {
				break;
			}
		}

		return milesDB;
	}

	@Override
	public List<Contributor> getAllContributors() {
		if (hostingMiner.getListener() != null) {
			hostingMiner.getListener().initMilestonesProcessing();
		}

		List<Contributor> contributors = new ArrayList<Contributor>();
		try {
			for (org.eclipse.egit.github.core.Contributor contributor : repoServ.getContributors(repositoryId, true)) {
				contributors.add(new Contributor(contributor.getName(), contributor.getLogin(),
						contributor.getAvatarUrl(), false));
			}

			for (User user : collaboratorServ.getCollaborators(repositoryId)) {
				for (Contributor contributor : contributors) {
					if (contributor.getLogin().equals(user.getLogin())) {
						contributor.setCollaborator(true);
						contributor.setEmail(user.getEmail());
					}
				}
			}

			return contributors;
		} catch (IOException e) {
			return contributors;
		}
	}

}