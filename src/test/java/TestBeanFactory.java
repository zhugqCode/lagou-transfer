import com.lagou.edu.factory.AnnotationBeanFactory;
import com.lagou.edu.service.TransferService;
import org.junit.Test;

public class TestBeanFactory {

    @Test
    public void testCreateBeanFactory(){
        TransferService transferService = (TransferService) AnnotationBeanFactory.getBean("transferService");
        System.out.println(transferService);
    }

}
