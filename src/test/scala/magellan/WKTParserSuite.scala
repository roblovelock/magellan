/**
  * Copyright 2015 Ram Sriharsha
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package magellan

import com.vividsolutions.jts.io.WKTReader
import org.scalatest.FunSuite

class WKTParserSuite extends FunSuite {

  test("parse int") {
    val parsed = WKTParser.int.parse("-30")
    assert(parsed.index === 3)
    assert(parsed.get.value === "-30")
  }

  test("parse float") {
    val parsed = WKTParser.float.parse("-79.470579")
    assert(parsed.get.value === "-79.470579")
  }

  test("parse number") {
    val parsed = WKTParser.number.parse("-79.470579")
    assert(parsed.get.value === -79.470579)
  }

  test("parse point") {
    val parsed = WKTParser.point.parse("POINT (30 10)")
    assert(parsed.index == 13)
    val p = parsed.get.value
    assert(p.getX() === 30.0)
    assert(p.getY() === 10.0)
  }

  test("parse empty point") {
    val parsed = WKTParser.pointEmpty.parse("POINT EMPTY")
    assert(parsed.index == 11)
    val p = parsed.get.value
    assert(p === NullShape)

  }

  test("parse multipoint, single value") {
    val parsed = WKTParser.multipoint.parse("MULTIPOINT (30 10)")
    assert(parsed.index == 18)
    val p = parsed.get.value

    assert(p.length === 1)

    assert(p(0).getX() === 30.0)
    assert(p(0).getY() === 10.0)
  }

  test("parse multipoint, two values") {
    val parsed = WKTParser.multipoint.parse("MULTIPOINT(30 10, 40 20)")
    assert(parsed.index == 24)
    val p = parsed.get.value

    assert(p.length === 2)

    assert(p(0).getX() === 30.0)
    assert(p(0).getY() === 10.0)
    assert(p(1).getX() === 40.0)
    assert(p(1).getY() === 20.0)
  }

  test("parse multipoint, two bracketed values") {
    val parsed = WKTParser.multipoint.parse("MULTIPOINT((30 10), (40 20))")
    assert(parsed.index == 28)
    val p = parsed.get.value

    assert(p.length === 2)

    assert(p(0).getX() === 30.0)
    assert(p(0).getY() === 10.0)
    assert(p(1).getX() === 40.0)
    assert(p(1).getY() === 20.0)
  }

  test("parse linestring") {
    var parsed = WKTParser.linestring.parse("LINESTRING (30 10, 10 30, 40 40)")
    var p: PolyLine = parsed.get.value
    assert(p.getNumRings() === 1)
    assert(p.length === 3)

    parsed = WKTParser.linestring.parse(
      "LINESTRING (-79.470579 35.442827,-79.469465 35.444889,-79.468907 35.445829,-79.468294 35.446608,-79.46687 35.447893)")

    p = parsed.get.value
    assert(p.length === 5)

  }

  test("parse multilinestring, single value") {
    val parsed = WKTParser.multilinestring.parse("MULTILINESTRING((30 10, 10 30, 40 40))")
    assert(parsed.index == 38)
    val p = parsed.get.value

    assert(p.length === 1)

    assert(p(0).length() === 3)
  }

  test("parse multilinestring, two values") {
    val parsed = WKTParser.multilinestring.parse("MULTILINESTRING((30 10, 10 30, 40 40),(-79.470579 35.442827,-79.469465 35.444889,-79.468907 35.445829,-79.468294 35.446608,-79.46687 35.447893))")
    assert(parsed.index == 144)
    val p = parsed.get.value

    assert(p.length === 2)

    assert(p(0).length() === 3)
    assert(p(1).length() === 5)
  }

  test("parse polygon without holes") {
    var parsed = WKTParser.polygonWithoutHoles.parse("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))")
    val p: Polygon = parsed.get.value
    assert(p.length === 5)
    assert(p.getNumRings() === 1)
    assert(p.getVertex(0) === Point(30, 10))
    assert(p.getVertex(1) === Point(40, 40))
    assert(p.getVertex(2) === Point(20, 40))
    assert(p.getVertex(3) === Point(10, 20))
    assert(p.getVertex(4) === Point(30, 10))
  }

  test("parse polygon with holes") {
    val parsed = WKTParser.polygonWithHoles.parse("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30))")
    val p: Polygon = parsed.get.value
    assert(p.getNumRings() == 2)
    assert(p.getRing(1) == 5)
    assert(p.getVertex(4) === Point(35.0, 10.0))
    assert(p.getVertex(5) === Point(20.0, 30.0))

  }

  test("parse Polygon without space") {
    val parsed = WKTParser.polygonWithHoles.parse("POLYGON((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30))")
    val p: Polygon = parsed.get.value
    assert(p.getNumRings() == 2)
    assert(p.getRing(1) == 5)
    assert(p.getVertex(4) === Point(35.0, 10.0))
    assert(p.getVertex(5) === Point(20.0, 30.0))
  }

  test("parse multipolygon") {
    val parsed = WKTParser.multipolygon.parse("MULTIPOLYGON(((30 10, 40 40, 20 40, 10 20, 30 10)), ((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30)))")
    val p = parsed.get.value

    assert(p.length === 2)

    assert(p(0).length === 5)
    assert(p(0).getNumRings() === 1)
    assert(p(0).getVertex(0) === Point(30, 10))
    assert(p(0).getVertex(1) === Point(40, 40))
    assert(p(0).getVertex(2) === Point(20, 40))
    assert(p(0).getVertex(3) === Point(10, 20))
    assert(p(0).getVertex(4) === Point(30, 10))

    assert(p(1).getNumRings() == 2)
    assert(p(1).getRing(1) == 5)
    assert(p(1).getVertex(4) === Point(35.0, 10.0))
    assert(p(1).getVertex(5) === Point(20.0, 30.0))
  }

  test("parse") {
    val shape = WKTParser.parseAll("LINESTRING (30 10, 10 30, 40 40)")
    assert(shape.isInstanceOf[PolyLine])
  }

  test("parse array") {
    val shape = WKTParser.parseAllArray("LINESTRING (30 10, 10 30, 40 40)")
    assert(shape.length === 1)
    assert(shape(0).isInstanceOf[PolyLine])
  }

  test("parse multi array") {
    val shape = WKTParser.parseAllArray("MULTILINESTRING ((30 10, 10 30, 40 40),(30 10, 10 30, 40 40))")
    assert(shape.length === 2)
    assert(shape(0).isInstanceOf[PolyLine])
    assert(shape(1).isInstanceOf[PolyLine])
  }

  test("parse failure") {
    try {
      WKTParser.parseAll("MULTILINESTRING ((30 10, 10 30, 40 40),30 10, 10 30, 40 40)")
      fail()
    }
    catch {
      case _: RuntimeException => // Expected, so continue
    }

  }


  test("parse array failure") {
    try {
      WKTParser.parseAll("LINESTRING (30 10, (10 30), 40 40)")
      fail()
    }
    catch {
      case _: RuntimeException => // Expected, so continue
    }

  }

  test("perf") {

    def time[R](block: => R): R = {
      val t0 = System.nanoTime()
      val result = block // call-by-name
      val t1 = System.nanoTime()
      println("Elapsed time: " + (t1 - t0) / 1E6 + "ms")
      result
    }

    def exec(text: String, n: Int, fn: (String) => Any) = {
      var i = 0
      while (i < n) {
        fn(text)
        i += 1
      }
    }

    val text = "LINESTRING (30 10, 10 30, 40 40)"
    val n = 100000

    time(exec(text, n, parseUsingJTS))
    time(exec(text, n, (s: String) => WKTParser.linestring.parse(s)))

  }

  private def parseUsingJTS(text: String): Shape = {
    val wkt = new WKTReader()
    val polyline = wkt.read(text)
    val coords = polyline.getCoordinates()
    val points = new Array[Point](coords.length)
    var i = 0
    while (i < coords.length) {
      val coord = coords(i)
      points.update(i, Point(coord.x, coord.y))
      i += 1
    }
    PolyLine(Array(0), points)
  }
}
