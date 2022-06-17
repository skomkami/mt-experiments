package pl.edu.agh.generator

import pl.edu.agh.model.Person
import faker._

import scala.util.Random
import scala.util.chaining._

object PeopleGenerator {

  val DIFFERENT_VALUES_NO = 1000

  def randomPerson(): Person = {
    val name = Random.nextInt(DIFFERENT_VALUES_NO) pipe firstNames
    val surname = Random.nextInt(DIFFERENT_VALUES_NO) pipe surnames
    val postalCode = Random.nextInt(DIFFERENT_VALUES_NO) pipe postalCodes
    val city = Random.nextInt(DIFFERENT_VALUES_NO) pipe cities
    val streetAddress = Random.nextInt(DIFFERENT_VALUES_NO) pipe streetAddresses

    Person(
      name = s"$name $surname",
      address = s"$postalCode $city, $streetAddress",
      email = s"$name.$surname@example.com".toLowerCase
    )
  }

  lazy val firstNames =
    (0 to DIFFERENT_VALUES_NO).map(_ => Faker.en.firstName())
  lazy val surnames = (0 to DIFFERENT_VALUES_NO).map(_ => Faker.en.lastName())
  lazy val postalCodes =
    (0 to DIFFERENT_VALUES_NO).map(_ => Faker.en.postalCode())
  lazy val cities = (0 to DIFFERENT_VALUES_NO).map(_ => Faker.en.city())
  lazy val streetAddresses =
    (0 to DIFFERENT_VALUES_NO).map(_ => Faker.en.streetAddress())
}
