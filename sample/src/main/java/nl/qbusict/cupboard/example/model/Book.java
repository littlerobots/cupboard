package nl.qbusict.cupboard.example.model;

public class Book {
    public static class ExtraInfo {
        public String info;
    }

    public Long _id;
    public String title;
    public Author author;
    public ExtraInfo info;
}
