package com.example.newsapi.model;

public class Views {
    public interface ShotUser {}
    public interface FullUser extends ShotUser {}
    public interface ShortLockAccount extends ShotUser {}
    public interface FullLockAccount extends ShortLockAccount {}
    public interface ShortNews extends ShotUser {}
    public interface FullNews {}
}
