import java.lang.reflect.Array;
import java.util.ArrayList;

// enemies.java
/*

1. Grup Üyesi: Oyun Mekanikleri ve Düşman Yönetimi (Game Mechanics & Enemy System)

Sorumluluklar:
	•	Düşman sınıfının yazılması (hareket, sağlık, ölüm animasyonu, para kazandırma).
	•	Düşmanların path üzerinde hareketini sağlayacak algoritmanın geliştirilmesi.
	•	Wave sisteminin (düşman dalgaları) dosyadan okunarak uygulanması.
	•	Düşmanların ekranda çizimi, sağlık barı animasyonları.
	•	Kısmi oyun döngüsü (enemy spawn ve update mekanikleri).

Dosya odaklı işler:
	•	WAVE_DATA parsing
	•	Enemy sınıfları (Circle/Polygon objeleri)
	•	Wave sistem zamanlayıcısı (delays, timers)

-Enemies can be drawn with Circle and Polygon shapes. You can also use an image asset to draw
enemy shape.
-The explosion effect when an enemy die should be implemented by animating 20 particles
drawn as small circles. A random angle and distance should be determined for each particle and
they should be animated going in that direction for 500 milliseconds


 */
public class enemies {
    public int health = 100;

    public enemies(ArrayList<Integer> list) {
        // initialize the enemies with the path they'll wander
        // list -> [[2],[4]], [], []

    }

    public void damageEnemy() {
        // decrease the health
        //health -= amount
    }

    public void dieExplode() {
        // when there are no health left explode and die
        // give 10 money to player
        // update money label
    }

    public void reachedEnd() {
        // not sure if this should be here
        // when reached the end decrease the players lives by 1
    }

}
