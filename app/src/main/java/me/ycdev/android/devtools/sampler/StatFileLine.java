package me.ycdev.android.devtools.sampler;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import me.ycdev.android.devtools.utils.Constants;
import me.ycdev.androidlib.utils.DateTimeUtils;

public class StatFileLine {
    public long timeUsage;
    public long cpuTime;
    public int processCount;
    public int memPss;
    public int memPrivate;
    public long trafficRecv;
    public long trafficSend;
}
