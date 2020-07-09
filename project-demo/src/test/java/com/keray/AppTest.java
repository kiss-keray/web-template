package com.keray;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keray.mapper.PlusMapper;
import com.keray.model.PlusModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebStart.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AppTest {

    @Resource
    private PlusMapper plusMapper;

    @Test
    public void selectOneTest() {
        System.out.println(plusMapper.selectOne(
                Wrappers.lambdaQuery(new PlusModel())
                .eq(PlusModel::getName, "keray")
        ));
    }
}
