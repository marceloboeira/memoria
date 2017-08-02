.PHONY: start
start: build
	target/universal/stage/bin/com-memoria

.PHONY: test
test:
	sbt test

.PHONY: spec
spec: test

.PHONY: build
build:
	sbt compile stage

.PHONY: epoch_now
epoch_now:
	date +%s

.PHONY: benchmark_upload
benchmark_upload:
	ab -n 1000 -c 50 -T "aplication/json" -p benchmark/post.json http://localhost:9000/upload

.PHONY: benchmark_statistics
benchmark_statistics:
	ab -n 1000 -c 50 http://localhost:9000/statistics
