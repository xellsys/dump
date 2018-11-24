import groovy.json.JsonSlurper
import groovy.transform.Field

import java.text.SimpleDateFormat

def slurper = new JsonSlurper()
def f = new File('/Users/benjaminh/Downloads/Takeout/Standortverlauf/Standortverlauf.json')

def json = slurper.parseText(f.text)

def reduced = json.locations[0..20000]

@Field
def format = new SimpleDateFormat("yyyy.MM.dd")

def locs = reduced.collect { location ->
    def ret = [:]
    ret.time = new Date(Long.valueOf(location.timestampMs))
    ret.date = format.format(ret.time)
    ret.dayOfWeek = ret.time.format('EEE')
    ret.lat = location.latitudeE7
    ret.long = location.longitudeE7
    return ret
}

@Field
def einstein = [lat: 481406370, long: 115229800]
//def einstein = [lat: 481404946, long: 115232348]
//48.140637, 11.522980


def atEinstein(def loc) {
    def acc = 10000
    def inLong = einstein.long - acc < loc.long && loc.long < einstein.long + acc
    def inLat = einstein.lat - acc < loc.lat && loc.lat < einstein.lat + acc

    def from = 6
    def till = 22
    def hoursOfDay = loc.time.hours
    def inTime = from < hoursOfDay && hoursOfDay < till

    def firstDate = new Date(2018, 07, 23)
    def inDate = loc.time.time > firstDate.time

    return inLat && inLong && inTime //&& inDate
}

def visits = locs.findAll { atEinstein(it) }.unique { it.date }.sort { it.date }
println visits.size()
println visits.join('\n')
println visits.collect { [it.date, it.dayOfWeek] }.join('\n')


