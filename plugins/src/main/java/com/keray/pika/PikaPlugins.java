package com.keray.pika;

import com.keray.IPlugins;
import com.keray.KouTuPlugins;
import org.springframework.stereotype.Service;

/**
 * @author by keray
 * date:2020/10/12 10:46 上午
 */
@Service("pikaPlugins")
public class PikaPlugins implements KouTuPlugins, IPlugins {

    @Override
    public String imageTrans(String url) {
        return url;
    }
}
