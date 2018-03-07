package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator

class SpringControllerDelegatingMethodTestGeneratorSpec extends MatcherPipelineTest {

    def "simple service call should be verified"() {
        expect:
        onClassCodeExpect """
        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
        
        @RestController
        public class SimpleDelegatingController {
        
            public final DelegateService delegateService;
        
            public SimpleDelegatingController(DelegateService delegateService) {
                this.delegateService = delegateService;
            }
        
            @RequestMapping("/delegate")
            public void delegate() {
                delegateService.delegate();
            }
        
        }

        """, """     
        package sample;

        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import org.junit.Test;
        import org.mockito.MockitoAnnotations;
        import org.junit.Before;
        import org.springframework.test.web.servlet.MockMvc;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.mockito.InjectMocks;
        import org.springframework.http.MediaType;
        import org.mockito.Mockito;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
        import org.mockito.Mock;
        import org.springframework.test.web.servlet.setup.MockMvcBuilders;
        
        public class FooTest {
        
            @Mock()
            private DelegateService delegateService;
        
            @InjectMocks()
            private SimpleDelegatingController controllerInstance;
        
            private MockMvc mockMvc;
        
            @Before
            public void setup() {
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(controllerInstance).build();
            }
        
            @Test
            public void test_delegate_DelegatesToService() throws Exception {
                mockMvc.perform(get("/delegate").accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().is2xxSuccessful());
                Mockito.verify(delegateService).delegate();
            }
        }
        """
    }

    @Override
    TestGenerator generator() {
        SpringControllerHelper controllerHelper = new SpringControllerHelper(solver, valueFactory)
        return new SpringControllerDelegatingMethodTestGenerator(solver, reporter, visitReporter, nomenclatureFactory,
                valueFactory, controllerHelper)
    }
}
