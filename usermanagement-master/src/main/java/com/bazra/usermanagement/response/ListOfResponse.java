package com.bazra.usermanagement.response;

import java.util.ArrayList;
import java.util.List;

public class ListOfResponse {
	private List<?> list = new ArrayList<>();
	public ListOfResponse(List<?> promotions) {
		this.list=promotions;
	}
	public List<?> getPromotions() {
		return list;
	}
	public void setPromotions(List<?> promotions) {
		this.list = promotions;
	}
}
