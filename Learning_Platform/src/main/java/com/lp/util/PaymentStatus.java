package com.lp.util;

public enum PaymentStatus {

	IN_PENDING(1, "PENDING"),CANCELED(2,"Cancelled"),IN_COMPLETE(3,"Completed"),FAILED(4, "Failed");

	private Integer id;

	private String name;

	private PaymentStatus(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}