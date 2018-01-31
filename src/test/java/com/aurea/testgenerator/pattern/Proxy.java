package com.aurea.testgenerator.pattern;

public class Proxy {

    final Proxied proxied = new Proxied();

    public void foo() {
        proxied.foo();

    }

    public void bar() {
        proxied.bar();

    }

    public void xyz() {
        proxied.xyz();
    }

    private class Proxied {

        public void foo() {

        }

        public void bar() {

        }

        public void xyz() {

        }
    }
}
