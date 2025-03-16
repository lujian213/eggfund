package io.github.lujian213.eggfund.dao

import spock.lang.Specification

abstract class FileSystemDaoSpec extends Specification {
    abstract File getTestDir();

    def cleanup() {
        getTestDir().deleteDir()
    }
}
