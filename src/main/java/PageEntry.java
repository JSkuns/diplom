import java.util.Comparator;

public class PageEntry implements Comparable<PageEntry> {

    private final String pdfName;
    private final int page;
    private final int count;

    PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    @Override
    public int compareTo(PageEntry o) {
        return Comparator.comparing((PageEntry p) -> p.count)
                .thenComparing(p-> p.page)
                .compare(o, this);
    }

    @Override
    public String toString() {
        return "PageEntry{" +
                "pdfName='" + pdfName + '\'' +
                ", page=" + page +
                ", count=" + count +
                "}";
    }

}
