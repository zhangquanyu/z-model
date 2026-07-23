package com.zmodel.repository;

import com.zmodel.entity.MethodParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodParamRepository extends JpaRepository<MethodParam, String> {

    List<MethodParam> findByMethodIdAndParamType(String methodId, String paramType);

    List<MethodParam> findByMethodId(String methodId);

    void deleteByMethodId(String methodId);

    void deleteByPropertyId(String propertyId);
}