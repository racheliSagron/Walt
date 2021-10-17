package com.awlt.message;

import java.util.Date;

public class ErrorMessage extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date date;
	private String message = "Error";

	public ErrorMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public ErrorMessage(Date date, String message) {
		this.date = date;
		this.message = message;
	}
	
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/*
	public String getMessage() {
		return message;
	}
	*/

	public void setMessage(String message) {
		this.message = message;
	}

	

}
