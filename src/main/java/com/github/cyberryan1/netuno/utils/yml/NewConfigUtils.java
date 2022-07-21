package com.github.cyberryan1.netuno.utils.yml;

import com.github.cyberryan1.cybercore.managers.FileType;
import com.github.cyberryan1.cybercore.managers.YMLManager;
import com.github.cyberryan1.cybercore.utils.yml.YMLReadTemplate;

public class NewConfigUtils extends YMLReadTemplate {

    public NewConfigUtils() {
        setYMLManager( new YMLManager( FileType.CONFIG ) );
    }
}