package com.hk.net;

public class Package {
    public PackageType type = PackageType.values()[0];
    public int length;
    public byte[] body;

    public Package(PackageType type, byte[] body) {
        this.type = type;
        this.length = body.length;
        this.body = body;
    }
}