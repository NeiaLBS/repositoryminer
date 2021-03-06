package org.repositoryminer.web.scm.model;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Milestone {

	private int number;
	private StatusType status;
	private String title;
	private String description;
	private String creator;
	private int openedIssues;
	private int closedIssues;
	private Date createdAt;
	private Date dueOn;
	private List<Integer> issues;
	private ObjectId repository;
	
	public Document toDocument() {
		Document doc = new Document();
		doc.append("number", number).append("status", status.toString()).append("title", title)
				.append("description", description).append("creator", creator).append("opened_issues", openedIssues)
				.append("closed_issues", closedIssues).append("created_at", createdAt).append("due_on", dueOn)
				.append("repository", repository).append("issues", issues);
		return doc;
	}

	public Milestone() {}
	
	public Milestone(int number, StatusType status, String title, String description, int openedIssues,
			int closedIssues, Date createdAt, Date dueOn) {
		super();
		this.number = number;
		this.status = status;
		this.title = title;
		this.description = description;
		this.openedIssues = openedIssues;
		this.closedIssues = closedIssues;
		this.createdAt = createdAt;
		this.dueOn = dueOn;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getOpenedIssues() {
		return openedIssues;
	}

	public void setOpenedIssues(int openedIssues) {
		this.openedIssues = openedIssues;
	}

	public int getClosedIssues() {
		return closedIssues;
	}

	public void setClosedIssues(int closedIssues) {
		this.closedIssues = closedIssues;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getDueOn() {
		return dueOn;
	}

	public void setDueOn(Date dueOn) {
		this.dueOn = dueOn;
	}

	public List<Integer> getIssues() {
		return issues;
	}

	public void setIssues(List<Integer> issues) {
		this.issues = issues;
	}

	public ObjectId getRepository() {
		return repository;
	}

	public void setRepository(ObjectId repository) {
		this.repository = repository;
	}

}