package edu.sjsu.cmpe.procurement.domain;

import java.util.ArrayList;

public class ShippedBooks {

	private ArrayList<Book> shipped_books = new ArrayList<Book>();

	public ArrayList<Book> getShipped_books() {
		return shipped_books;
	}

	public void setShipped_books(ArrayList<Book> shipped_books) {
		this.shipped_books = shipped_books;
	}
	
	public int getNumBooks (){
		return shipped_books.size();
	}
}
