package mds.streamingserver;

import mds.streamingserver.components.MyResourceHttpRequestHandler;
import mds.streamingserver.model.MovieLibrary;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static mds.streamingserver.FilePaths.*;

@Controller
public class WebController {

    private MyResourceHttpRequestHandler handler;

    @Autowired
    public WebController(MyResourceHttpRequestHandler handler){
        this.handler = handler;
    }

    @GetMapping("video")
    public String video(){
        return "videoMP4";
    }

    @GetMapping(path = "file", produces = "video/mp4")
    @ResponseBody
    public FileSystemResource file(){
        return new FileSystemResource(MP4_FILE);
    }

    @GetMapping("byterange")
    public void byterange(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, MP4_FILE);
        handler.handleRequest(request, response);
    }

    @GetMapping ("index")
    public String index(){
        return "index";
    }

    @RequestMapping(path = "/player", method = {RequestMethod.GET, RequestMethod.POST})
    public String player(Model model, @RequestParam String URL, @RequestParam(defaultValue = "1000px") String width, @RequestParam(defaultValue = "false") boolean muted, @RequestParam(defaultValue = "false") boolean autoplay){
        model.addAttribute("URL", URL);
        model.addAttribute("width", width);
        model.addAttribute("muted", muted ? "true" : "");
        model.addAttribute("autoplay", autoplay ? "true" : "");

    if(StringUtils.isEmpty(URL)){
        return "error";
    }

    return "player";
    }



    @RequestMapping(value = {"/dash/{file}","/hls/{file}","/hls/{quality}/{file}"}, method = RequestMethod.GET)
    public void adaptive_streaming(@PathVariable String file,
    @PathVariable(required = false) String quality,
    HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException{
      File STREAM_FILE = null;

      String handle = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

      switch (handle){
          case "/dash/{file}":
              STREAM_FILE = new File(DASH_PATH + file);
              break;
          case "/hls/{file}":
              STREAM_FILE = new File(HLS_PATH + file);
              break;
          case "/hls/{quality/{file}":
              STREAM_FILE = new File(HLS_PATH  + quality + file );
              break;
          //default: ;

      }

      request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE,STREAM_FILE);
      handler.handleRequest(request, response);
    }

    @RequestMapping(value = "dashPlayer", method = {RequestMethod.GET, RequestMethod.POST})
    public String dashPath(@RequestParam String url, Model model){
        //if(StringUtils.isEmpty(url)){
            model.addAttribute("url", url);
        //}


        return "dashPlayer";
    }


    private MovieLibrary library = null;
    @RequestMapping(value = "gallery", method = {RequestMethod.GET, RequestMethod.POST})
    public String gallery(Model model) throws JCodecException, IOException {
        if(library==null){
            library = new MovieLibrary(IMAGES_DIRECTORY, MP4_DIRECTORY, SUFFIX, 150);
        }
        model.addAttribute("movies", library);

        return "gallery";
    }

    @GetMapping ("/video/{file}")
    public String showVideo(Model model, @PathVariable String file){
        model.addAttribute("movieName", file);
        return "video";
    }

    @GetMapping("/getvideo/{file}")
    public void getVideo(HttpServletRequest request, HttpServletResponse response, @PathVariable String file) throws ServletException, IOException {
        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, new File(MP4_DIRECTORY, file));
        handler.handleRequest(request, response);
    }

}
