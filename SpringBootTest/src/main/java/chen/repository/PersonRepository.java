package chen.repository;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import chen.domain.Person;

@Repository
@CacheConfig(cacheNames = "person")
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByName(String name);

    List<Person> findByAddress(String address);

    List<Person> findByNameAndAddress(String name, String address);

    @Query("select p from Person p where p.department.id = :id")
    List<Person> findByDepartmentId(@Param("id") Long id);
    
    List<Person> findByDepartment_Name(String name);
    
    List<Person> findByCourses_Name(String name);

    @Cacheable(value="bynameandage",key = "#name+'-'+#age")//保存缓存
    @Query("select p from Person p where p.name=:name and p.age=:age")
    List<Person> withNameAndAddressQuery(@Param("name") String name, @Param("age") Integer age);
    
    @CacheEvict(value="bynameandage",key = "#id")//清除缓存
    @Query("select p from Person p ,Course c where c.id = :id")
    List<Person> findByCoureseId(@Param("id") Long id);
}
