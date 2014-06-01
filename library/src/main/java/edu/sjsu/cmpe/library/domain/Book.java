package edu.sjsu.cmpe.library.domain;

import java.net.MalformedURLException;
import java.net.URL;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class Book {

    public enum Status {
	available("available"), checkedout("checkedout"), lost("lost");

	private String value;

	Status(String value) {
	    this.value = value;
	}

	public String getValue() {
	    return value;
	}

	@Override
	public String toString() {
	    return this.getValue();
	}

	public static Status getEnum(String value) {
	    if (value == null) {
		throw new IllegalArgumentException();
	    }
	    for (Status v : values()) {
		if (value.equalsIgnoreCase(v.getValue())) {
		    return v;
		}
	    }
	    throw new IllegalArgumentException();
	}
    }

    @NotNull
    private long isbn;

    @NotEmpty
    private String title;

    @NotEmpty
    private String category;

    private URL coverimage;

    /** Default is 'available' */
    private Status status = Status.available;

    /**
     * @return the isbn
     */
    public long getIsbn() {
	return isbn;
    }

    /**
     * @param isbn
     *            the isbn to set
     */
    public void setIsbn(long isbn) {
	this.isbn = isbn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @return the category
     */
    public String getCategory() {
	return category;
    }

    /**
     * @param category
     *            the category to set
     */
    public void setCategory(String category) {
	this.category = category;
    }

    /**
     * @return the coverimage
     */
    public URL getCoverimage() {
	return coverimage;
    }

    /**
     * @param coverimage
     *            the coverimage to set
     */
    public void setCoverimage(URL coverImage) {
	this.coverimage = coverImage;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
	return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status) {
	this.status = status;
    }
    
 public void addNewBook (String newBook){
    	
    	String[] bookDetails = newBook.replace(":/", "~/").split(":");
    	this.isbn=Integer.parseInt(bookDetails[0]);
    	this.title=bookDetails[1];
    	this.category=bookDetails[2];
    	String url = bookDetails[3].replace("~/", ":/");
    	try {
			this.coverimage=new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }

}
