package Crawler;

import java.util.ArrayList;

public class Url_Data {
    // represents the url of a web page
    //private String anchor;
    // represents the text in html tags
    private ArrayList<String> text;
    // represents the priority of every text based on the html tag
    private ArrayList<Integer> tags;


    /**
     * constructor to initialize the object and attach the anchor to it
     *
     * @return no return value
     */
    public Url_Data() {

        this.text = new ArrayList<String>();
        this.tags = new ArrayList<Integer>();
    }

    /**
     * add an element to text arraylist
     *
     * @param text
     * @return void
     */
    public void addText(String text) {
        this.text.add(text);
    }

    /**
     * add a priority refers to a specific text in text arraylist
     *
     * @param priority
     * @return void
     */
    public void addPriority(int priority) {
        this.tags.add(priority);
    }

    /**
     * set text array with the url text data
     * @param text
     */
    public void addText(ArrayList<String> text) {
        this.text = text;
    }

    /**
     * set tags array with the url tags
     * @param tags
     */

    public void addTag(ArrayList<Integer> tags) {
        this.tags = tags;
    }

    /**
     * getting the size of any arraylist
     *
     * @return integer
     */
    public int getSize() {
        return text.size();
    }

    /**
     * get text array
     * @return ArrayList<String>
     */
    public ArrayList<String> getText() {
        return this.text;
    }

    /**
     * get tags array
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getTags() {
        return this.tags;
    }

    public int getTextSize() {
        return this.text.size();
    }
}
