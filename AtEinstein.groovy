import groovy.json.JsonSlurper
import groovy.transform.Field

import java.text.SimpleDateFormat

def slurper = new JsonSlurper()
def f = new File('/Users/benjaminh/Downloads/Takeout/Standortverlauf/Standortverlauf.json')

def json = slurper.parseText(f.text)

def reduced = json.locations

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
@Field
Date firstDate = new Date(118, 06, 23)


def atEinstein(def loc) {
    def acc = 10000
    def inLong = einstein.long - acc < loc.long && loc.long < einstein.long + acc
    def inLat = einstein.lat - acc < loc.lat && loc.lat < einstein.lat + acc

    def from = 6
    def till = 22
    def hoursOfDay = loc.time.hours
    def inTime = from < hoursOfDay && hoursOfDay < till

    def inDate = ((Date) loc.time).after firstDate

    return inLat && inLong && inTime && inDate
}

def visits = locs.findAll { atEinstein(it) }.unique { it.date }.sort { it.date }

def priceYear = 550
def numVisits = visits.size()
def priceEntry = (550 / numVisits).round(2)
def regularEntry = 11.90
def savedEntry = (regularEntry - priceEntry).round(2)
def totalSave = regularEntry * numVisits - priceYear


println """You have been to Einstein on ${numVisits} days since ${format.format(firstDate)}
You have paid ${priceYear}€ for one year.
You have paid ${priceEntry}€ for each entry and saved ${savedEntry}€ on each visit.
Saved in total compared to paying each entry individually: ${totalSave}€.
Those are the dates: \n${visits.collect { [it.date, it.dayOfWeek] }.join(' ')}"""