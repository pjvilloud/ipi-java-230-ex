package com.ipiecoles.java.java230.service;

import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EmployeServiceTest {

    @Autowired
    private EmployeService employeService;

    @Autowired
    private EmployeRepository employeRepository;

    @Test
    public void exo301testInit() throws Exception {
        TestUtils.checkNotAbstractClass("EmployeService");
        Field field = TestUtils.checkPrivateField("EmployeService", "employeRepository", TestUtils.PACKAGE_REPOSITORY + "EmployeRepository");
        Assertions.assertThat(field.isAnnotationPresent(Autowired.class)).isTrue();
    }

    @Test
    public void exo302testFindById(){
        //Given

        //When
        Employe e = employeService.findById(2L);

        //Then
        Assertions.assertThat(e).isNotNull();
        Assertions.assertThat(e.getMatricule()).isEqualTo("M11109");

    }

    @Test
    public void exo303testNombreEmploye(){
        //Given

        //When
        Long c = employeService.countAllEmploye();

        //Then
        Assertions.assertThat(c).isNotNull();
        Assertions.assertThat(c).isEqualTo(2502L);

    }

    @Test
    public void exo304testcreerEmploye(){
        //Given
        Employe c = new Commercial("test", "test", "test", LocalDate.now(), 500d, 0d);

        //When
        c = employeService.creerEmploye(c);

        //Then
        Assertions.assertThat(c.getId()).isNotNull();

        //TearDown
        employeRepository.deleteById(c.getId());

    }

    @Test
    public void exo305testDeleteEmploye(){
        //Given
        final Commercial c = employeRepository.save(new Commercial("test", "test", "test", LocalDate.now(), 500d, 0d));

        //When
        employeService.deleteEmploye(c.getId());

        //Then
        Assertions.assertThatThrownBy(() -> employeService.findById(c.getId())).isInstanceOf(EntityNotFoundException.class);

    }

}