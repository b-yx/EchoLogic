package org.example.dao.service.impl;

import org.example.dao.mapper.TagxMapper;
import org.example.dao.pojo.Tagx;
import org.example.dao.service.TagxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagxServiceImpl implements TagxService {

    @Autowired
    private TagxMapper tagMapper;

    @Override
    public List<Tagx> findAll() {
        return tagMapper.findAll();
    }

    @Override
    public Tagx findById(Integer id) {
        return tagMapper.findById(id);
    }

    @Override
    public void createTag(Tagx tag) {
        tagMapper.insert(tag);
    }

    @Override
    public void updateTag(Tagx tag) {
        tagMapper.update(tag);
    }

    @Override
    public void deleteTag(Integer id) {
        tagMapper.delete(id);
    }

    @Override
    public List<Tagx> findTagsByRecordId(Integer recordId) {
        return tagMapper.findByRecordId(recordId);
    }
}