import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    List<PageEntry> entryList = new ArrayList<>(); // здесь хранятся ответы на запрос
    Map<String, Integer> frequency = new HashMap<>(); // ключ -> слово; значение -> поисковый ответ
    Map<String, PageEntry> mapForEachPage = new TreeMap<>(); // здесь будем хранить необходимые данные для поиска
    List<Map<String, PageEntry>> mapList = new ArrayList<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        // получим список путей к каждому пдф-файлу из данной нам папки
        List<File> fileList = new ArrayList<>();
        for (File file : Objects.requireNonNull(pdfsDir.listFiles())) {
            if (file.isFile())
                fileList.add(file);
        }
        // считываем каждый пдф-документ и делаем из них объекты
        List<PdfDocument> pdfObjList = new ArrayList<>();
        for (File pdfFile : fileList) {
            // создать объект пдф-документа
            pdfObjList.add(new PdfDocument(new PdfReader(pdfFile)));
        }
        // разбираем каждый пдф-документ на страницы, затем считываем из них текст
        for (PdfDocument doc : pdfObjList) {
            // количество страниц в текущей пдф-ке
            int numberOfPages = doc.getNumberOfPages();
            // перебираем все страницы в текущей пдф-ке и считываем текст
            for (int j = 1; j <= numberOfPages; j++) {
                // получаем объект каждой страницы
                PdfPage page = doc.getPage(j);
                // получить текст со страницы
                var text = PdfTextExtractor.getTextFromPage(page);
                // разбить текст на слова
                var words = text.split("\\P{IsAlphabetic}+");
                // определим встречаемость слова в тексте на данной странице
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    frequency.put(word.toLowerCase(), frequency.getOrDefault(word, 0) + 1);
                }
                // название документа
                String docName = doc.getDocumentInfo().getTitle();
                // добавляем нужные нам элементы
                for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
                    mapForEachPage.put(entry.getKey(), new PageEntry(docName, j, entry.getValue()));
                }
                mapList.add(mapForEachPage);
                mapForEachPage = new TreeMap<>();
                frequency.clear();
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        for (Map<String, PageEntry> entryMap : mapList) {
            for (Map.Entry<String, PageEntry> entry : entryMap.entrySet()) {
                if (word.equals(entry.getKey())) {
                    entryList.add(entry.getValue());
                }
            }
        }
        Collections.sort(entryList);
        return entryList;
    }
}
