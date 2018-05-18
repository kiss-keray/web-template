package com.nix.common.captcha;


import com.nix.common.EncriptionKit;
import com.nix.common.captcha.background.BackgroundFactory;
import com.nix.common.captcha.color.ColorFactory;
import com.nix.common.captcha.color.RandomColorFactory;
import com.nix.common.captcha.filter.predefined.*;
import com.nix.common.captcha.font.RandomFontFactory;
import com.nix.common.captcha.sevice.Captcha;
import com.nix.common.captcha.sevice.ConfigurableCaptchaService;
import com.nix.common.captcha.text.renderer.BestFitTextRenderer;
import com.nix.common.captcha.text.renderer.TextRenderer;
import com.nix.common.captcha.word.RandomWordFactory;
import com.nix.service.impl.SystemService;
import com.nix.common.EncriptionKit;
import com.nix.service.impl.SystemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Created by wangrenhui on 13-12-31.
 */
public class CaptchaRender {

  private Log logger = LogFactory.getLog(getClass());
  private HttpServletRequest request;
  private HttpServletResponse response;
  public CaptchaRender(HttpServletRequest request, HttpServletResponse response){
    this.request = request;
    this.response = response;
  }

  private String code = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
  private int font_min_num = 4;
  private int font_max_num = 4;
  private int font_min_size = 20;
  private int font_max_size = 20;
  private int top_margin = 1;
  private int bottom_margin = 1;
  private int width = 118;
  private int height = 41;
  private ConfigurableCaptchaService configurableCaptchaService = new ConfigurableCaptchaService();
  private ColorFactory colorFactory = null;
  private RandomFontFactory fontFactory = null;
  private RandomWordFactory wordFactory = null;
  private TextRenderer textRenderer = null;
  private BackgroundFactory backgroundFactory = null;
  //滤镜特效
  private FilterFactory filter = null;
  private static Random random = new Random();
  /**
   * 背景色
   */
  private Color bgColor = null;

  /**
   * 验证码字符颜色
   */
  private Color drawColor = new Color(0, 0, 0);
  /**
   * 背景元素的颜色
   */
  private Color drawBgColor = new Color(102, 102, 102);

  private boolean randomColor = false;
  /**
   * 噪点数量
   */
  private int artifactNum = 50;

  private int lineNum = 0;

  private void initCaptchService() {

    // 颜色创建工厂,使用一定范围内的随机色

    if (randomColor) {
      colorFactory = new RandomColorFactory();
    } else {
      colorFactory = new ColorFactory() {

        @Override
        public Color getColor(int index) {
          return drawColor;//new Color(118,102,102);
        }
      };
    }

    configurableCaptchaService.setColorFactory(colorFactory);

    // 随机字体生成器
    fontFactory = new RandomFontFactory();
    fontFactory.setMaxSize(font_max_size);
    fontFactory.setMinSize(font_min_size);
    configurableCaptchaService.setFontFactory(fontFactory);

    // 随机字符生成器,去除掉容易混淆的字母和数字,如o和0等
    wordFactory = new RandomWordFactory();
    wordFactory.setCharacters(code);
    wordFactory.setMaxLength(font_max_num);
    wordFactory.setMinLength(font_min_num);
    configurableCaptchaService.setWordFactory(wordFactory);

    // 自定义验证码图片背景
    if (backgroundFactory == null) {
      backgroundFactory = new SimpleBackgroundFactory(bgColor, randomColor ? null : drawBgColor, artifactNum, lineNum);
    }
    configurableCaptchaService.setBackgroundFactory(backgroundFactory);

    // 图片滤镜设置
    int filterNum;
    if (filter == null) {
      filterNum = random.nextInt(4);
    } else {
      filterNum = filter.value();
    }

    switch (filterNum) {
      case 0:
        configurableCaptchaService.setFilterFactory(new CurvesRippleFilterFactory(configurableCaptchaService.getColorFactory()));
        break;
      case 1:
        configurableCaptchaService.setFilterFactory(new MarbleRippleFilterFactory());
        break;
      case 2:
        configurableCaptchaService.setFilterFactory(new DoubleRippleFilterFactory());
        break;
      case 3:
        configurableCaptchaService.setFilterFactory(new WobbleRippleFilterFactory());
        break;
      case 4:
        configurableCaptchaService.setFilterFactory(new DiffuseRippleFilterFactory());
        break;
      default:
        //默认效果
        configurableCaptchaService.setFilterFactory(new CurvesRippleFilterFactory(configurableCaptchaService.getColorFactory()));
        break;
    }

    // 文字渲染器设置
    textRenderer = new BestFitTextRenderer();
    textRenderer.setBottomMargin(bottom_margin);
    textRenderer.setTopMargin(top_margin);
    configurableCaptchaService.setTextRenderer(textRenderer);

    // 验证码图片的大小
    configurableCaptchaService.setWidth(width);
    configurableCaptchaService.setHeight(height);
  }

  /**
   * you can  rewrite this  render
   * 输出
   */
  public void render() {
    //初始化
    initCaptchService();
    ServletOutputStream outputStream = null;

    // 得到验证码对象,有验证码图片和验证码字符串
    Captcha captcha = configurableCaptchaService.getCaptcha();
    // 取得验证码字符串放入Session
    String captchaCode = captcha.getChallenge();
    logger.debug("captcha:" + captchaCode);
    HttpSession session = request.getSession();
    session.setAttribute(SystemService.CAPTCHA_NAME, EncriptionKit.encrypt(captchaCode.toLowerCase()));
    session.setAttribute(SystemService.CAPTCHA_NAME + "_time", System.currentTimeMillis());
//    CookieUtils.addCookie(request, response, AppConstants.CAPTCHA_NAME, EncriptionKit.encrypt(captchaCode), -1);
    // 取得验证码图片并输出
    BufferedImage bufferedImage = captcha.getImage();

    try {
      outputStream = response.getOutputStream();
      ImageIO.write(bufferedImage, "png", outputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.flush();
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  public BackgroundFactory getBackgroundFactory() {
    return backgroundFactory;
  }

  public void setBackgroundFactory(BackgroundFactory backgroundFactory) {
    this.backgroundFactory = backgroundFactory;
  }

  public Color getDrawColor() {
    return drawColor;
  }

  public void setDrawColor(Color drawColor) {
    this.drawColor = drawColor;
  }

  public Color getDrawBgColor() {
    return drawBgColor;
  }

  public void setDrawBgColor(Color drawBgColor) {
    this.drawBgColor = drawBgColor;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setFontNum(int font_min_num, int font_max_num) {
    this.font_min_num = font_min_num;
    this.font_max_num = font_max_num;
  }


  public void setFontSize(int font_min_size, int font_max_size) {
    this.font_min_size = font_min_size;
    this.font_max_size = font_max_size;
  }

  public void setFontMargin(int top_margin, int bottom_margin) {
    this.top_margin = top_margin;
    this.bottom_margin = bottom_margin;
  }

  public void setImgSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void setArtifactNum(int artifactNum) {
    this.artifactNum = artifactNum;
  }

  public void setLineNum(int lineNum) {
    this.lineNum = lineNum;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setFilter(FilterFactory filter) {
    this.filter = filter;
  }

  public void setRandomColor(boolean randomColor) {
    this.randomColor = randomColor;
  }

  public enum FilterFactory {
    //曲面
    Curves(0),
    //大理石纹
    Marble(1),
    //对折
    Double(2),
    //颤动
    Wobble(3),
    //扩散
    Diffuse(4);
    private int value;

    private FilterFactory(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }
  }


  public static void main(String[] args) {
    int imgWidth = 400;
    int imgHeight = 300;
    File file = new File("E:/test/1.png");
    BufferedImage image = new BufferedImage(imgWidth, imgHeight,
        BufferedImage.TYPE_INT_ARGB);//RGB形式
    BackgroundFactory bf = new SimpleBackgroundFactory(new Color(255, 255, 0, 100));
    bf.fillBackground(image);
    try {
      ImageIO.write(image, "png", file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
