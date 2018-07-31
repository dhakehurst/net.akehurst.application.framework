package net.akehurst.application.framework.common.realisation;

import net.akehurst.application.framework.common.ActiveProcessor;

public class test_AsyncProcessor {

	ActiveProcessor sut1;
	ActiveProcessor sut2;

	void f() {

		this.sut1.send(Ping.class, Ping::ping, "Hello!");

		this.sut2.receive1(Ping.class, Ping::ping, (p1) -> {

		});

	}
}
