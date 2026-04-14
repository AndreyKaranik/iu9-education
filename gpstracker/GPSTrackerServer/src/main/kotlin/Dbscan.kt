import kotlinx.serialization.Serializable
import kotlin.math.*

data class Point(val lat: Double, val lon: Double, var clusterId: Int = -1)

@Serializable
data class ClusterCenter(val latitude: Double, val longitude: Double)

fun haversine(p1: Point, p2: Point): Double {
    val R = 6371.0
    val dLat = Math.toRadians(p2.lat - p1.lat)
    val dLon = Math.toRadians(p2.lon - p1.lon)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(p1.lat)) * cos(Math.toRadians(p2.lat)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

fun dbscan(points: List<Point>, epsKm: Double, minPts: Int): List<Pair<Double, Double>> {
    var clusterId = 0
    for (p in points) {
        if (p.clusterId != -1) continue
        val neighbors = points.filter { haversine(p, it) <= epsKm }
        if (neighbors.size < minPts) {
            p.clusterId = -2
        } else {
            clusterId++
            expandCluster(points, p, neighbors, clusterId, epsKm, minPts)
        }
    }

    return (1..clusterId).map { id ->
        val cluster = points.filter { it.clusterId == id }
        val avgLat = cluster.map { it.lat }.average()
        val avgLon = cluster.map { it.lon }.average()
        avgLat to avgLon
    }
}

fun expandCluster(points: List<Point>, point: Point, neighbors: List<Point>, clusterId: Int, epsKm: Double, minPts: Int) {
    point.clusterId = clusterId
    val queue = neighbors.toMutableList()
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current.clusterId == -1 || current.clusterId == -2) {
            current.clusterId = clusterId
            val currentNeighbors = points.filter { haversine(current, it) <= epsKm }
            if (currentNeighbors.size >= minPts) {
                queue.addAll(currentNeighbors.filter { it.clusterId == -1 || it.clusterId == -2 })
            }
        }
    }
}