import org.otfried.cs109ui.*
import java.awt.image.BufferedImage
import org.otfried.cs109ui.ImageCanvas
import org.otfried.cs109.Color
import org.otfried.cs109.DrawStyle

data class Pos(val x: Int, val y: Int) {
  fun dx(d: Int): Pos = Pos(x + d, y)
  fun dy(d: Int): Pos = Pos(x, y + d)
}

class Block(p: Pos) {
  val a = mutableListOf(p)
  override fun toString(): String = 
    if (a.size==1) "Block{Pos(x=${a[0].x}, y=${a[0].y})}" 
	else "Block{Pos(x=${a[0].x}, y=${a[0].y}), Pos(x=${a[1].x}, y=${a[1].y})}"
  fun positions(): List<Pos> = a
  fun isStanding(): Boolean = (positions().size==1)
  fun left() {
    if (isStanding()) {
	  a[0] = a[0].dx(-2)
	  a.add(Pos(a[0].x+1, a[0].y))
	} else if (!isStanding() && a[0].x < a[1].x) {
	  a[0] = a[0].dx(-1)
	  a.removeAt(1)
	  } else if (!isStanding()) {
	  a[0] = a[0].dx(-1)
	  a[1] = a[1].dx(-1)
	  }
  }
  fun right() {
    if (isStanding()) {
	  a[0] = a[0].dx(1)
	  a.add(Pos(a[0].x+1, a[0].y))
	} else if (!isStanding() && a[0].x < a[1].x) {
	  a[0] = a[0].dx(2)
	  a.removeAt(1)
	  } else if (!isStanding()) {
	  a[0] = a[0].dx(1)
	  a[1] = a[1].dx(1)
	  }
  }  
  fun up() {
    if (isStanding()) {
	  a[0] = a[0].dy(-2)
	  a.add(Pos(a[0].x, a[0].y+1))
	} else if (!isStanding() && a[0].x < a[1].x) {
	  a[0] = a[0].dy(-1)
	  a[1] = a[1].dy(-1)
	  } else if (!isStanding()) {
	  a[0] = a[0].dy(-1)
	  a.removeAt(1)
	  }
  }
  fun down() {
    if (isStanding()) {
	  a[0] = a[0].dy(1)
	  a.add(Pos(a[0].x, a[0].y+1))
	} else if (!isStanding() && a[0].x < a[1].x) {
	  a[0] = a[0].dy(1)
	  a[1] = a[1].dy(1)
	  } else if (!isStanding()) {
	  a[0] = a[0].dy(2)
	  a.removeAt(1)
	  }
  }
}

class Terrain(fname: String) {
  val list = java.io.File(fname).readLines()
  fun start(): Pos {
    for (i in  0..list.size-1)
	  for (j in 0..list[i].length-1)
	    if (list[i][j] == 'S') return Pos(j,i)
	return Pos(0,0)
  }
  fun target(): Pos {
    for (i in  0..list.size-1)
	  for (j in 0..list[i].length-1)
	    if (list[i][j] == 'T') return Pos(j,i)
    return Pos(0,0)
  }
  fun width(): Int = list.sortedBy{-it.length}[0].length
  fun height(): Int = list.size 
  fun at(p: Pos): Int {
    val m = mutableMapOf <Pos, Int> ()
    for (i in 0..(height()-1))
      for (j in 0..(width()-1)) {
	    if (j > (list[i].length-1) || list[i][j]=='-') m[Pos(j,i)] = 0
	    else if (list[i][j]=='o' || list[i][j]=='S' || list[i][j]=='T') m[Pos(j, i)] = 2
	    else if (list[i][j]=='.') m[Pos(j,i)] = 1
	}
	if (p.x<0 || p.y<0 || p.x>(width()-1)|| p.y>(height()-1))
	  return 0
	return m[p]!!
  }
  fun canHold(b: Block): Boolean = 
    if (b.positions().size==1) (at(b.positions()[0])!=0 && at(b.positions()[0])!=1)
	else  (at(b.positions()[0])!=0 && at(b.positions()[1])!=0)
}

fun draw(c: ImageCanvas, t: Terrain, ts: Double, b: Block) {
  c.clear(Color.GRAY)
  for (i in 0..(t.width()-1))
    for (j in 0..(t.height()-1)) {
      if (b.positions().size==1 && b.positions()[0] == Pos(i,j)) {
	    c.setColor(Color.RED)
        c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL)
	  }
	  else if (b.positions().size==2 && b.positions()[0] == Pos(i,j)) {
	    c.setColor(Color.RED)
        c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL) 
	  }
	  else if (b.positions().size==2 && b.positions()[1] == Pos(i,j)) {
	    c.setColor(Color.RED)
        c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL) 
	  }
	  else if (t.at(Pos(i,j))==2 && Pos(i,j)!=t.target()) {
        c.setColor(Color.BLUE)
        c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL)
	  }
	  else if (t.at(Pos(i,j))==2 && Pos(i,j)==t.target()) {
	    c.setColor(Color.GREEN)
        c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL)
      }
	  else if (t.at(Pos(i,j))==1) {
	    c.setColor(Color.CYAN)
		c.drawRectangle(ts*i, ts*j, ts-3, ts-3, DrawStyle.FILL)
	  }
	}
}

fun makeMove(b: Block, ch: Char) {
  when (ch) {
    's' -> b.down()
	'w' -> b.up()
	'd' -> b.right()
	'a' -> b.left()
  }
}

fun tileSize(t: Terrain): Int {
  var ts = 60
  while (ts > 5) {
    if (t.width() * ts <= 800 && t.height() * ts <= 640)
      return ts
    ts -= 2
  }
  return ts
}
  
fun playLevel(level: Int) {
  val terrain = Terrain("level%02d.txt".format(level))
  val ts = tileSize(terrain)
  val image = BufferedImage(ts * terrain.width() + 20, ts * terrain.height() + 20, BufferedImage.TYPE_INT_RGB)
  val canvas = ImageCanvas(image)
  var block = Block(terrain.start())
  var moves = 0

  while (true) {
    setTitle("Bloxorz Level $level ($moves moves)")
    draw(canvas, terrain, ts.toDouble(), block)
    show(image)
    val ch = waitKey()
    if (ch in "swda") {
      makeMove(block, ch)
      moves += 1
    }
    if (block.isStanding() && block.positions().first() == terrain.target()) {
      showMessage("Congratulations, you solved level $level")
      return
    }
    if (!terrain.canHold(block)) {
      showMessage("You fell off the terrain")
      block = Block(terrain.start())
    }
  }
}

fun main(args: Array<String>) {
  if (args.size == 1) {
    playLevel(args[0].toInt())
	var i = 1
	while ((args[0].toInt()+i)<10) {
	  playLevel(args[0].toInt()+i)
	  i += 1
	}
  }
  else 
    println("Please enter the level number from 1 to 9")
}