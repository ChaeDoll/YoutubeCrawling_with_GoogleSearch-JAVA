import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;

class Music{
    private String image;
    private String title;
    private String url;
    public void setImage(String image) {
        this.image = image;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String toString(){
        return "{title:"+this.title+", url:"+this.url+", image:"+this.image+"}";
    }
}

public class Crawling {
    public static void main(String[] args) throws IOException {
        Music music = new Music();
        // 예시 코드입니다. "폴킴 - 너를 만나" 공간에 원하는 검색어(문자열)을 입력하세요.
        String encodedKeyword = URLEncoder.encode("폴킴 - 너를 만나", "UTF-8");
        String crawlingURL = "https://www.google.com/search?q=" + encodedKeyword + "&tbm=vid";
        Document document = Jsoup.connect(crawlingURL).get();
        Element title = document.selectFirst("#rso > div:nth-child(1) > div > div > div > div > div > div:nth-child(1) > div.xe8e1b > div > div > span > a > h3");
        Element url = document.selectFirst("#rso > div:nth-child(1) > div > div > div > div > div > div:nth-child(1) > div.xe8e1b > div > div > span > a");
        Elements images = document.select("script");
        for (Element image : images){
            String imageData = image.data();
            if (imageData.contains("data:image/jpeg;base64")){
                int startIndex = imageData.indexOf("data:image/jpeg;base64");
                int endIndex = imageData.indexOf("';var");
                if (startIndex != -1) {
                    String base64Image = imageData.substring(startIndex, endIndex);
                    music.setImage(base64Image);
                    break;
                }
            }
        }
        music.setTitle(title.text());
        music.setUrl(url.attr("href"));
        System.out.println(music);
    }
}
