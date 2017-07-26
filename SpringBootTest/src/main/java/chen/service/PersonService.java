package chen.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chen.domain.Department;
import chen.domain.Person;
import chen.repository.PersonRepository;

@Service
//@Transactional(propagation=Propagation.REQUIRED)//事务传播性
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

//     @Transactional
    public void save(Person p) {
        personRepository.save(p);

        Person p1 = new Person();
        p1.setId((long) 6);
        p1.setName("chen");
        p1.setAge(28);
        Department dp = new Department();
        dp.setName("12");
        p1.setDepartment(dp);
        personRepository.save(p1);
    }

    public List<Person> find(String name, Integer age) {
        return personRepository.withNameAndAddressQuery(name, age);
    }

    public List<Person> findByDepartmentId(Long id) {
        return personRepository.findByDepartmentId(id);
    }

    public List<Person> findByDepartment_Name(String name) {
        return personRepository.findByDepartment_Name(name);
    }

    public List<Person> findByCourse_Name(String name) {
        return personRepository.findByCourses_Name(name);
    }

    public List<Person> findByCoureseId(Long name) {
        return personRepository.findByCoureseId(name);
    }
}
