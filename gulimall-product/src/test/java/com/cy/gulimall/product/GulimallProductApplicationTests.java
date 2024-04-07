package com.cy.gulimall.product;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.cy.gulimall.product.dao.AttrGroupDao;
import com.cy.gulimall.product.dao.SkuSaleAttrValueDao;
import com.cy.gulimall.product.entity.BrandEntity;
import com.cy.gulimall.product.service.BrandService;
import com.cy.gulimall.product.service.SkuSaleAttrValueService;
import com.cy.gulimall.product.vo.SkuItemVo;
import com.netflix.client.ClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private RedissonClient client;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test01() {
        List<SkuItemVo.SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(13L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }
    @Test
    public void test02() {
        List<SkuItemVo.SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(13L);
        System.out.println(saleAttrsBySpuId);

    }
    @Test
    public void testRedisson() {
        System.out.println(client);
    }

//    @Autowired
//    private OSS ossClient;
//    @Test
//    public void testUpdate() throws FileNotFoundException {
//        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5t9LdJDecCoZDoZoqBbVv";
//        String accessKeySecret = "ygXzaZ3DCxuS5h456gTKD3mXjPKIRx";
//        // 填写Bucket名称，例如examplebucket。
//        String bucketName = "cyjoker-edu";
//        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
//        String objectName = "OIP.jpg";
//        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
//        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        String filePath= "C:\\Users\\chen'yi\\OneDrive\\桌面\\OIP.jpg";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        try {
//            InputStream inputStream = new FileInputStream(filePath);
//            // 创建PutObjectRequest对象。
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
//            // 设置该属性可以返回response。如果不设置，则返回的response为空。
//            putObjectRequest.setProcess("true");
//            // 创建PutObject请求。
//            PutObjectResult result = ossClient.putObject(putObjectRequest);
//            // 如果上传成功，则返回200。
//            System.out.println(result.getResponse().getStatusCode());
//        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
//        } catch (ClientException ce) {
//            System.out.println("Caught an ClientException, which means the client encountered "
//                    + "a serious internal problem while trying to communicate with OSS, "
//                    + "such as not being able to access the network.");
//            System.out.println("Error Message:" + ce.getMessage());
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//    }

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("成功");
    }

}
