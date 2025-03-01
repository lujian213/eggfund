package io.github.lujian213.eggfund.dao;

import java.io.File;

public class FileSystemDaoImpl {
    protected File repoFile;

    public FileSystemDaoImpl(File repoFile) {
        this.repoFile = repoFile;
        init();
    }

    protected void init() {
        if (!repoFile.exists()) {
            repoFile.mkdirs();
        }
    }
}