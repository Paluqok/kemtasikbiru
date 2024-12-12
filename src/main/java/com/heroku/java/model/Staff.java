package com.heroku.java.model;

public class Staff {
	private Long staffId;
	private String staffName;
	private String staffEmail;
	private String staffPhoneNo;
	private String staffAddress;
	private String staffPassword;
	private Integer managerId;
	
	public Staff() {}
	
	public Staff(Long staffId,String staffName,String staffEmail,String staffPhoneNo,String staffAddress,String staffPassword,Integer managerId) {
		this.staffId=staffId;
		this.staffName=staffName;
		this.staffEmail=staffEmail;
		this.staffPhoneNo=staffPhoneNo;
		this.staffAddress=staffAddress;
		this.staffPassword=staffPassword;
		this.managerId=managerId;
	}
	
	
	public Long getStaffId() {
		return staffId;
	}
	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	public String getStaffEmail() {
		return staffEmail;
	}
	public void setStaffEmail(String staffEmail) {
		this.staffEmail = staffEmail;
	}
	public String getStaffPhoneNo() {
		return staffPhoneNo;
	}
	public void setStaffPhoneNo(String staffPhoneNo) {
		this.staffPhoneNo = staffPhoneNo;
	}
	public String getStaffAddress() {
		return staffAddress;
	}
	public void setStaffAddress(String staffAddress) {
		this.staffAddress = staffAddress;
	}
	public String getStaffPassword() {
		return staffPassword;
	}
	public void setStaffPassword(String staffPassword) {
		this.staffPassword = staffPassword;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}
	
	
}