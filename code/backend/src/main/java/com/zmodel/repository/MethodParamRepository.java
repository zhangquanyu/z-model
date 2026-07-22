package com.zmodel.repository;

import com.zmodel.entity.MethodParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodParamRepository extends JpaRepository<MethodParam, Long> {

    List<MethodParam> findByMethodId(Long methodId);

    List<MethodParam> findByMethodIdAndParamType(Long methodId, String paramType);

    void deleteByMethodId(Long methodId);

    void deleteByPropertyId(Long propertyId);
}
