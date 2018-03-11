package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.MatcherPipelineTest
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.value.VariableFactoryReplacingMocksByNewInstances

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
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.junit.Before;
        import org.junit.Test;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.MockitoAnnotations;
        import org.springframework.http.MediaType;
        import org.springframework.test.web.servlet.MockMvc;
        import org.springframework.test.web.servlet.setup.MockMvcBuilders;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
        import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
         
        public class FooTest {
         
            @InjectMocks()
            private SimpleDelegatingController controllerInstance;
         
            @Mock()
            private DelegateService delegateService;
         
            private MockMvc mockMvc;
         
            @Before
            public void setup() {
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(controllerInstance).build();
            }
         
            @Test
            public void test_delegate_DelegatesToService() throws Exception {
                mockMvc.perform(get("/delegate")).andExpect(status().is2xxSuccessful());
                Mockito.verify(delegateService).delegate();
            }
        }
        """
    }

    def "service call with parameters should be verified"() {
        expect:
        onClassCodeExpect """
        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;
        
        @RestController
        @RequestMapping(path = "/params")
        public class DelegatingWithParametersController {
        
            public final DelegateService delegateService;
        
            public DelegatingWithParametersController(DelegateService delegateService) {
                this.delegateService = delegateService;
            }
        
            @PostMapping("/delegate/{int_param}")
            public void delegate(@PathVariable(name = "int_param") int intParam, @RequestParam(name = "string_param") String
                    stringParm) {
                delegateService.delegateWithParameters(intParam, stringParm);
            }
        
        }

        """, """ 
        package sample;
 
        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.junit.Before;
        import org.junit.Test;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.MockitoAnnotations;
        import org.springframework.http.MediaType;
        import org.springframework.test.web.servlet.MockMvc;
        import org.springframework.test.web.servlet.setup.MockMvcBuilders;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;
        import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
         
        public class FooTest {
         
            @InjectMocks()
            private DelegatingWithParametersController controllerInstance;
         
            @Mock()
            private DelegateService delegateService;
         
            private MockMvc mockMvc;
         
            @Before
            public void setup() {
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(controllerInstance).build();
            }
         
            @Test
            public void test_delegate_DelegatesToService() throws Exception {
                int intParam = 42;
                String stringParm = "ABC";
                mockMvc.perform(post("/params/delegate/" + intParam + "").param("string_param", String.valueOf(stringParm))).andExpect(status().is2xxSuccessful());
                Mockito.verify(delegateService).delegateWithParameters(eq(intParam), eq(stringParm));
            }
        }
        """
    }

    def "service call returning values should be mocked"() {
        expect:
        onClassCodeExpect """
        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;
        
        @RestController
        @RequestMapping(value = "/return")
        public class DelegatingWithReturnValueController {
        
            public final DelegateService delegateService;
        
            public DelegatingWithReturnValueController(DelegateService delegateService) {
                this.delegateService = delegateService;
            }
        
            @RequestMapping(value = "/delegate/{int_param}", method = RequestMethod.POST)
            public Value delegateWithValue(@PathVariable(name = "int_param") int intParam,
                    @RequestParam(name = "string_param") String stringParm, @RequestBody Body body) {
                return delegateService.delegateWithValue(intParam, stringParm, body);
            }
        
        }
        
         class Value {
            private int inntValue;
            private String stringValue;
        
            public Value(int inntValue, String stringValue) {
                this.inntValue = inntValue;
                this.stringValue = stringValue;
            }
        
            public Value(){}
        
            public int getInntValue() {
                return inntValue;
            }
        
            public void setInntValue(int inntValue) {
                this.inntValue = inntValue;
            }
        
            public String getStringValue() {
                return stringValue;
            }
        
            public void setStringValue(String stringValue) {
                this.stringValue = stringValue;
            }
        }
        
        class Body {
            int id;
        
            public Body(int id) {
                this.id = id;
            }
        
            public Body() {
            }
        
            public int getId() {
                return id;
            }
        
            public void setId(int id) {
                this.id = id;
            }
        }
        
        class DelegateService {
            public Value delegateWithValue(int intParam, String stringParm, Body body) {
                return new Value(intParam,stringParm);
            }
        }

        """, """ 
        package sample;
 
        import com.aurea.auth.generalpatternsspringtest.service.DelegateService;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import org.junit.Before;
        import org.junit.Test;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.MockitoAnnotations;
        import org.springframework.http.MediaType;
        import org.springframework.test.web.servlet.MockMvc;
        import org.springframework.test.web.servlet.setup.MockMvcBuilders;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;
        import static org.mockito.Mockito.*;
        import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
        import static org.mockito.Mockito.mock;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
         
        public class FooTest {
         
            @InjectMocks()
            private DelegatingWithReturnValueController controllerInstance;
         
            @Mock()
            private DelegateService delegateService;
         
            private MockMvc mockMvc;
         
            @Before
            public void setup() {
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(controllerInstance).build();
            }
         
            @Test
            public void test_delegateWithValue_DelegatesToService() throws Exception {
                int intParam = 42;
                String stringParm = "ABC";
                Body body = new Body();
                ObjectMapper mapper = new ObjectMapper();
                Value expectedResult = new Value();
                Mockito.when(delegateService.delegateWithValue(eq(intParam), eq(stringParm), any(Body.class))).thenReturn(expectedResult);
                mockMvc.perform(post("/return/delegate/" + intParam + "").content(mapper.writeValueAsString(body)).contentType("application/json;charset=UTF-8").param("string_param", String.valueOf(stringParm))).andExpect(status().is2xxSuccessful());
                Mockito.verify(delegateService).delegateWithValue(eq(intParam), eq(stringParm), any(Body.class));
            }
        }
        """
    }

    @Override
    TestGenerator generator() {
        return new SpringControllerDelegatingMethodTestGenerator(solver, reporter, visitReporter, nomenclatureFactory,
                valueFactory)
    }
}
