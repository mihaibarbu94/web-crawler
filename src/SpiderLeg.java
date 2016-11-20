import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SpiderLeg {
    // Use a fake USER_AGENT so the web server thinks this is a normal web browser.
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; " +
            "WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 " +
            "Safari/535.1";
    private transient String domain;
    private String url;
    private Set<String> assets = new HashSet<>();

    public SpiderLeg(String domain){
        this.domain = domain;
    }

    /**
     * This performs all the work. It makes an HTTP request, checks the
     * response, and then gather all the links on the page. Perform a
     * searchForWord after the successful crawl
     *
     * @param url - The URL to visit
     * @return whether or not the crawl was successful
     */
    public Set<String> crawl(String url, Spider spider) {
        this.url = url;
        Set<String> urls = new HashSet<>();
        //System.out.println("DOmain        " + domain + "\n");

        if (url.startsWith(domain)) {
            try {
                Connection connection = Jsoup.connect(url).timeout(2*1000)
                        .userAgent
                        (USER_AGENT);
                Document htmlDocument = connection.get();
                if (connection.response().statusCode() != 200) {
                    // 200 is the HTTP OK status code indicating if it is ok.
                    System.out.println("\nError visiting " + url);
                }

                Elements links   = htmlDocument.select("a[href]");
                Elements media   = htmlDocument.select("img");
                Elements imports = htmlDocument.select("script");

                System.out.println("Found (" + links.size() + ") links");
                for (Element link : links) {
                    //System.out.println("links");
                    if(link.absUrl("abs:href").startsWith(domain)){
                        if (!link.absUrl("abs:href").contains("/#")) {
                            urls.add(link.absUrl("abs:href"));
                        }
                    }
                }

                for (Element image : media) {
                    //System.out.println("media");
                    assets.add(image.absUrl("abs:src"));
                }

                for (Element image : imports) {
                    //System.out.println("imports");
                    assets.add(image.absUrl("abs:href"));
                }
                spider.setAssets(assets);
                Gson gson = new Gson();
                System.out.println(gson.toJson(this));
                return urls;
            } catch (IOException ioe) {
                // We were not successful in our HTTP request
                System.out.println(ioe);
                System.out.println("\nSomething went wrong here: " + url);
            }
        }
        return urls;
    }
}