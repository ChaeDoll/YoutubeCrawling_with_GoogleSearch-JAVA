# Youtube Crawling with Google search (Java Jsoup Library)
> 자바(Java) Jsoup 라이브러리를 통해 구글 검색결과를 크롤링하여 유튜브 정보 가져오기  
> Using Java Jsoup Library, Youtube Crawling (thumbnail, title, url, etc..) with Google Search

### 🔽 예시
![image](https://github.com/ChaeDoll/Java-Youtube_Crawling_with_Google_search/assets/108540812/7e0e655a-1d93-4c89-89ad-7295d933c0dc)

### 🔽 코드
```
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

```
### 설명
- Java 환경에서 Jsoup 라이브러리를 통해 google 검색결과 창에 검색어를 집어넣어 Crawling합니다.
- 얻어낸 정적요소에서 document.select 메소드를 통해 원하는 값이 존재하는 selector 값을 입력합니다.
- h3 태그로 이루어진 Youtube title 에서는 내부에 있는 텍스트 요소를 얻어내야 하기에 title.text()를 통해 값을 얻어왔습니다.
- a 태그로 이루어진 Youtube link url 에서는 속성에 있는 href의 값을 얻어내야 하기에 url.attr("href")를 통해 값을 얻어왔습니다.
- img 태그의 경우에 정적요소에서 값을 얻어내면 blurImage(임시 이미지)가 들어가있었기에, script 내부에 존재하는 image값을 가져와야 했습니다.  
  그리하여 script 태그를 선택한 뒤, script에 적힌 글을 먼저 image.data()를 통해 문자열로 가져왔습니다.
  이후 Youtube 썸네일 이미지의 형태인 "data:image/jpeg;base64"를 포함하는 문자열들 중, "data:image/jpeg;base64"로 시작하고 "';var"이전에 끝나는 문자열을 substring 메소드를 통해 가져왔습니다.

### ETC
- 위 코드는 현재 Repository에 올라가 있는 Java 코드입니다.
- Spring 환경에서는 Jsoup 라이브러리를 추가해준 뒤 (dependency), 해당 코드를 활용할 수 있습니다.
- Java 환경에서는 현재 Repository에 올라가 있는 jsoup.jar을 현재 모듈에 직접 import하여 라이브러리를 추가할 수 있습니다.


- Spring을 통해 Frontend와 연결하게 된다면 위 코드를 활용하여 서비스를 만들고, Getmapping 혹은 Postmapping을 통해 Frontend에게 api 요청을 받을 때 해당 서비스를 실행시켜준 뒤, Response Data로 원하는 결과를 return하여 이를 구현할 수 있습니다.
```
// Backend 코드
@RestController
public class MusicController {
    private final CrawlingService crawlingService;
    @Autowired
    public MusicController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }
    @PostMapping("/api/music")
    public ResponseDto getMusicList(@RequestBody RequestMusicDto requestMusicDto) throws IOException {
        List<Music> musicList = crawlingService.getMusicList(requestMusicDto.getSongList());
        return new ResponseDto(HttpStatus.OK.value(), "음악 데이터 목록입니다.", musicList);
    }
}

@Service
public class CrawlingService {
    public List<Music> getMusicList(String songList) throws IOException {
        List<Music> musicList = new ArrayList<>();
        List<String> recommendList = List.of(songList.split("\\n"));
        for (String recommend:recommendList) {
            Music music = new Music();
            String encodedKeyword = URLEncoder.encode(recommend, "UTF-8");
            String crawlingURL = "https://www.google.com/search?q=" + encodedKeyword + "&tbm=vid";
            Document document = Jsoup.connect(crawlingURL).get();
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
            music.setTitle(recommend);
            music.setUrl(url.attr("href"));
            musicList.add(music);
        }
        return musicList;
    }
}
```
  
