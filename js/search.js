// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "ADT support",
      "url": "/slog4s/adt.html",
      "content": "ADT support slog4s provides built-in support for automatic derivation of LogEncoder typeclass, which allows one to use case class or sealed trait as additional arguments for logging. It supports both fully automatic derivation, and semi automatic derivation. Installation libraryDependencies ++= Seq(\"com.avast\" %% \"slog4s-generic\" % \"0.6.1+7-cb5b3c2d-SNAPSHOT\") Example Suppose we have following case class: case class Foo(fooValue: String) case class Bar(barValue: Int, foo: Foo) val bar = Bar(42, Foo(\"Hello!\")) Automatic derivation With automatic derivation you just need to include proper import. import slog4s.generic.auto._ logger.info .withArg(\"bar\", bar) .log(\"Logging bar instance\") .unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:18.704+01:00\", \"@version\" : \"1\", \"bar\" : { \"barValue\" : 42, \"foo\" : { \"fooValue\" : \"Hello!\" } }, \"file\" : \"adt.md\", \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 43, \"logger_name\" : \"test-logger\", \"message\" : \"Logging bar instance\", \"thread_name\" : \"Thread-17\" } Semi automatic derivation Sometimes it might be more convenient to have LogEncoder instance have defined directly in a code. You can use semi automatic derivation for that: import slog4s.generic.semi._ object Foo { implicit val fooEncoder: LogEncoder[Foo] = logEncoder[Foo] } object Bar { implicit val barEncoder: LogEncoder[Bar] = logEncoder[Bar] } logger.info .withArg(\"bar\", bar) .log(\"Logging bar instance\") .unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:18.725+01:00\", \"@version\" : \"1\", \"bar\" : { \"barValue\" : 42, \"foo\" : { \"fooValue\" : \"Hello!\" } }, \"file\" : \"adt.md\", \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 65, \"logger_name\" : \"test-logger\", \"message\" : \"Logging bar instance\", \"thread_name\" : \"Thread-17\" } Map support There is built-in support for representing Maps. Generally we try to represent them as a map in the target encoding (think JSON dictionary) whenever possible. However it might be impossible for cases where a key is not a primitive type or a String. So there is a simple rule: if the key implements cats.Show typeclass, we represent the whole Map as a real map/dictionary. Otherwise we represent it as an array of key/value pairs. import cats.Show import cats.instances.all._ case class MyKey(value: String) object MyKey { implicit val showInstance: Show[MyKey] = _.value } logger.info .withArg(\"foo\", Map(\"key\" -&gt; \"value\")) .withArg(\"bar\", Map(42 -&gt; \"value\")) .withArg(\"baz\", Map(MyKey(\"my_key\") -&gt; \"value\")) .log(\"Hello world\") .unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:18.9+01:00\", \"@version\" : \"1\", \"bar\" : { \"42\" : \"value\" }, \"baz\" : { \"my_key\" : \"value\" }, \"file\" : \"adt.md\", \"foo\" : { \"key\" : \"value\" }, \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 90, \"logger_name\" : \"test-logger\", \"message\" : \"Hello world\", \"thread_name\" : \"Thread-17\" } case class OtherKey(x: Int, y: Int) logger.info .withArg(\"foo\", Map(OtherKey(1,2) -&gt; \"value\")) .log(\"Hello world\") .unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:18.903+01:00\", \"@version\" : \"1\", \"file\" : \"adt.md\", \"foo\" : [ [ { \"x\" : 1, \"y\" : 2 }, \"value\" ] ], \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 102, \"logger_name\" : \"test-logger\", \"message\" : \"Hello world\", \"thread_name\" : \"Thread-17\" }"
    } ,    
    {
      "title": "Contextual logging",
      "url": "/slog4s/contextual-logging.html",
      "content": "Contextual logging Contextual logging is provided by LoggingContext typeclass. LoggingContext is a typeclass, as its name suggest, provides propagation of logging context across multiple loggers and log messages. It allows you to add additional arguments to all log messages produced inside given code block. Typical example is Correlation-Id (sometimes also known as Trace-Id) HTTP header. When an HTTP request comes in, we assign unique random string to it (Correlation-Id). Using LoggingContext we can easily make sure that this string appears in all log messages related to the HTTP request. There are plenty use cases where you might want to include common argument in all related log messages: user id, file name or any other entity. Here is a simple example: import cats.Monad import cats.syntax.all._ import slog4s._ def foo[F[_]:Monad:LoggingContext](loggerFactory: LoggerFactory[F]): F[Unit] = { val logger = loggerFactory.make(\"foo\") LoggingContext[F].withArg(\"correlation_id\", \"generated-correlation-id\") .use { logger.info(\"Hello from foo!\") &gt;&gt; bar(loggerFactory) } } def bar[F[_]:LoggingContext](loggerFactory: LoggerFactory[F]): F[Unit] = { val logger = loggerFactory.make(\"bar\") logger.info(\"Hellow from bar!\") } No we just need to get LoggingContext and LoggerFactory instances. We will use slf4j module for that. We will also use ReaderT[IO, Slf4jArgs, ?] as our effect type because the implementation requires our effect implement ApplicationLocal typeclass. There are more efficient implementations for different effect types that relies on other mechanisms (for curious ones: it TaskLocal for Monix and FiberRef for ZIO) import cats.data._ import cats.effect._ import cats.mtl.instances.local._ import slog4s.shared._ import slog4s.slf4j._ type Result[T] = ReaderT[IO, Slf4jArgs, T] val loggingRuntime = Slf4jFactory[Result].make(ContextRuntime[Result, Slf4jArgs]) import loggingRuntime._ No we can finally run it: // we start with empty additional arguments foo(loggerFactory).run(Slf4jArgs.empty).unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:19.521+01:00\", \"@version\" : \"1\", \"correlation_id\" : \"generated-correlation-id\", \"file\" : \"contextual-logging.md\", \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 30, \"logger_name\" : \"foo\", \"message\" : \"Hello from foo!\", \"thread_name\" : \"Thread-18\" }"
    } ,    
    {
      "title": "Integrations",
      "url": "/slog4s/integrations/",
      "content": "Integrations slog4s provides integrations to various libraries. Monix ZIO"
    } ,    
    {
      "title": "Home",
      "url": "/slog4s/",
      "content": "This is a quick start with slog4s on the JVM with logback backend. It doesn’t mean we are limited to JVM or logback! Au contraire! Installation Add new library dependencies to your build.sbt libraryDependencies ++= Seq(\"com.avast\" %% \"slog4s-api\" % \"0.6.1+7-cb5b3c2d-SNAPSHOT\", \"com.avast\" %% \"slog4s-slf4j\" % \"0.6.1+7-cb5b3c2d-SNAPSHOT\") We will be using logback with logstash encoder. So make sure it’s in your dependency list and it’s configured properly. &lt;configuration&gt; &lt;appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\"&gt; &lt;encoder class=\"net.logstash.logback.encoder.LogstashEncoder\" /&gt; &lt;/appender&gt; &lt;root level=\"INFO\"&gt; &lt;appender-ref ref=\"STDOUT\" /&gt; &lt;/root&gt; &lt;/configuration&gt; Usage Obligatory imports before we start: import cats.effect._ import cats.syntax.all._ import slog4s._ import slog4s.slf4j._ Let’s start by creating a LoggerFactory that is backed by slf4j (and logback under the hood). With that, we can create a named Logger. val loggerFactory = Slf4jFactory[IO].withoutContext.loggerFactory // loggerFactory: LoggerFactory[IO] = slog4s.slf4j.Slf4jFactory$WithoutContextBuilder$$anon$3@42e4d8c val logger = loggerFactory.make(\"test-logger\") // logger: Logger[IO] = slog4s.slf4j.Slf4jLogger@6b182cdf It’s finally time to log something! logger.info(\"Hello world!\").unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:19.522+01:00\", \"@version\" : \"1\", \"correlation_id\" : \"generated-correlation-id\", \"file\" : \"contextual-logging.md\", \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 37, \"logger_name\" : \"bar\", \"message\" : \"Hellow from bar!\", \"thread_name\" : \"Thread-18\" } That was pretty boring, except file and line attributes that denote location of the log message within a file. We can also provide an exception: logger.error(new Exception(\"Boom!\"), \"Something went horribly wrong.\").unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:19.931+01:00\", \"@version\" : \"1\", \"file\" : \"index.md\", \"level\" : \"INFO\", \"level_value\" : 20000, \"line\" : 41, \"logger_name\" : \"test-logger\", \"message\" : \"Hello world!\", \"thread_name\" : \"Thread-19\" } Let’s make our message more content rich with additional arguments: logger.info .withArg(\"string_value\", \"&lt;VALUE&gt;\") .withArg(\"bool_value\", true) .withArg(\"list_value\", List(1,2,3)) .log(\"Message with arguments\") .unsafeRunSync() Output: { \"@timestamp\" : \"2023-02-24T17:57:19.934+01:00\", \"@version\" : \"1\", \"file\" : \"index.md\", \"level\" : \"ERROR\", \"level_value\" : 40000, \"line\" : 47, \"logger_name\" : \"test-logger\", \"message\" : \"Something went horribly wrong.\", \"stack_trace\" : \"java.lang.Exception: Boom!\\n\\tat repl.MdocSession$MdocApp.&lt;init&gt;(index.md:47)\\n\\tat repl.MdocSession$.app(index.md:3)\\n\\tat mdoc.internal.document.DocumentBuilder$$doc$.$anonfun$build$2(DocumentBuilder.scala:89)\\n\\tat scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)\\n\\tat scala.util.DynamicVariable.withValue(DynamicVariable.scala:62)\\n\\tat scala.Console$.withErr(Console.scala:196)\\n\\tat mdoc.internal.document.DocumentBuilder$$doc$.$anonfun$build$1(DocumentBuilder.scala:89)\\n\\tat scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)\\n\\tat scala.util.DynamicVariable.withValue(DynamicVariable.scala:62)\\n\\tat scala.Console$.withOut(Console.scala:167)\\n\\tat mdoc.internal.document.DocumentBuilder$$doc$.build(DocumentBuilder.scala:88)\\n\\tat mdoc.internal.markdown.MarkdownBuilder$.$anonfun$buildDocument$2(MarkdownBuilder.scala:47)\\n\\tat mdoc.internal.markdown.MarkdownBuilder$$anon$1.run(MarkdownBuilder.scala:104)\\n\", \"thread_name\" : \"Thread-19\" } Additional arguments are type safe. Following code will fail in compile time: class That(value: String) logger.info .withArg(\"that_value\", new That(\"value\")) .log(\"Does not compile\") .unsafeRunSync() // error: could not find implicit value for evidence parameter of type slog4s.LogEncoder[repl.MdocSession.MdocApp.That] // logger.info // ^ However there is built-in support for case classes and sealed traits provided by generic module."
    } ,    
    {
      "title": "Monix",
      "url": "/slog4s/integrations/monix.html",
      "content": "Monix Module slog4s-monix provides basic integration with monix effect system. It leverages TaskLocal for context propagation. slf4j example This example demonstrates how to make an slf4j specific instance of LoggingContext and LoggerFactory backed by monix’s TaskLocal. import monix.eval._ import slog4s._ import slog4s.shared._ import slog4s.monix._ import slog4s.slf4j._ Slf4jFactory[Task].makeFromBuilder(MonixContextRuntimeBuilder) // res0: Task[LoggingRuntime[Task]] = Map( // Map( // ContextSwitch( // Eval(monix.eval.TaskLocal$$$Lambda$8061/0x00000008025f7b10@5bd13960), // monix.eval.TaskLocal$$$Lambda$8060/0x00000008025f7558@2c63d803, // null // ), // slog4s.monix.MonixContextRuntime$$$Lambda$8062/0x00000008025f2000@5a6749a4, // StackTrace( // List( // monix.eval.internal.TaskTracing$.buildFrame(TaskTracing.scala:52), // monix.eval.internal.TaskTracing$.buildCachedFrame(TaskTracing.scala:43), // monix.eval.internal.TaskTracing$.cached(TaskTracing.scala:38), // monix.eval.Task.map(Task.scala:2027), // slog4s.monix.MonixContextRuntime$.make(MonixContextRuntime.scala:17), // slog4s.monix.MonixContextRuntimeBuilder$.make(MonixContextRuntimeBuilder.scala:9), // slog4s.monix.MonixContextRuntimeBuilder$.make(MonixContextRuntimeBuilder.scala:6), // slog4s.slf4j.Slf4jFactory$Slf4jFactoryBuilder.makeFromBuilder(Slf4jFactory.scala:52), // repl.MdocSession$MdocApp.&lt;init&gt;(monix.md:23), // repl.MdocSession$.app(monix.md:3), // mdoc.internal.document.DocumentBuilder$$doc$.$anonfun$build$2(DocumentBuilder.scala:89), // scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23), // scala.util.DynamicVariable.withValue(DynamicVariable.scala:62), // scala.Console$.withErr(Console.scala:196), // mdoc.internal.document.DocumentBuilder$$doc$.$anonfun$build$1(DocumentBuilder.scala:89), // scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23), // scala.util.DynamicVariable.withValue(DynamicVariable.scala:62), // scala.Console$.withOut(Console.scala:167), // mdoc.internal.document.DocumentBuilder$$doc$.build(DocumentBuilder.scala:88), // mdoc.internal.markdown.MarkdownBuilder$.$anonfun$buildDocument$2(MarkdownBuilder.scala:47), // mdoc.internal.markdown.MarkdownBuilder$$anon$1.run(MarkdownBuilder.scala:104) // ) // ) // ), // slog4s.slf4j.Slf4jFactory$Slf4jFactoryBuilder$$Lambda$8063/0x00000008025f2cc0@268fa0ce, // StackTrace( // List( // monix.eval.internal.TaskTracing$.buildFrame(TaskTracing.scala:52), // monix.eval.internal.TaskTracing$.buildCachedFrame(TaskTracing.scala:43), // monix.eval.internal.TaskTracing$.cached(TaskTracing.scala:38), // ..."
    } ,    
    {
      "title": "Overview",
      "url": "/slog4s/overview.html",
      "content": "Modules overview slog4s is split into multiple modules: slog4s-api: top level API that should be used by libraries slog4s-console: simple console based implementation of the API slog4s-generic: brings support for automatic derivation for ADT slog4s-monix: provides implementation of LoggingContext specialised for Monix slog4s-slf4j: slf4j and logstash-encoder specific implementation of the logging API slog4s-testkit: provides mock implementation of API that can be used by unit tests"
    } ,      
    {
      "title": "ZIO",
      "url": "/slog4s/integrations/zio.html",
      "content": "Monix Module slog4s-zio provides basic integration with ZIO effect system. It leverages FiberRef for context propagation. slf4j example This example demonstrates how to make an slf4j specific instance of LoggingContext and LoggerFactory backed by ZIO’s FiberRef. import cats.effect.Sync import slog4s._ import slog4s.shared._ import slog4s.slf4j._ import slog4s.zio._ import _root_.zio._ def make(implicit F: Sync[Task]): Task[LoggingRuntime[Task]] = { Slf4jFactory[Task].makeFromBuilder(ZIOContextRuntimeBuilder.Task) }"
    }    
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
