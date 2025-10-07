import androidx.health.connect.client.records.ExerciseSessionRecord

object ExerciseTypeMapper {

    private val exerciseTypeMap = mapOf(
        (ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON to "Badminton"),
        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL to "Baseball",
        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL to "Basketball",
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING to "Biking",
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY to "Stationary Biking",
        ExerciseSessionRecord.EXERCISE_TYPE_BOOT_CAMP to "Boot Camp",
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING to "Boxing",
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS to "Calisthenics",
        ExerciseSessionRecord.EXERCISE_TYPE_CRICKET to "Cricket",
        ExerciseSessionRecord.EXERCISE_TYPE_DANCING to "Dancing",
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL to "Elliptical",
        ExerciseSessionRecord.EXERCISE_TYPE_FENCING to "Fencing",
        ExerciseSessionRecord.EXERCISE_TYPE_FRISBEE_DISC to "Frisbee",
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AMERICAN to "Football (American)",
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AUSTRALIAN to "Football (Australian)",
        ExerciseSessionRecord.EXERCISE_TYPE_GOLF to "Golf",
        ExerciseSessionRecord.EXERCISE_TYPE_GUIDED_BREATHING to "Guided Breathing",
        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS to "Gymnastics",
        ExerciseSessionRecord.EXERCISE_TYPE_HANDBALL to "Handball",
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING to "HIIT",
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING to "Hiking",
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_HOCKEY to "Ice Hockey",
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_SKATING to "Ice Skating",
        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS to "Martial Arts",
        ExerciseSessionRecord.EXERCISE_TYPE_PADDLING to "Paddling",
        ExerciseSessionRecord.EXERCISE_TYPE_PARAGLIDING to "Paragliding",
        ExerciseSessionRecord.EXERCISE_TYPE_PILATES to "Pilates",
        ExerciseSessionRecord.EXERCISE_TYPE_RACQUETBALL to "Racquetball",
        ExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING to "Rock Climbing",
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING to "Rowing",
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE to "Rowing Machine",
        ExerciseSessionRecord.EXERCISE_TYPE_RUGBY to "Rugby",
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING to "Running",
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL to "Treadmill Running",
        ExerciseSessionRecord.EXERCISE_TYPE_SAILING to "Sailing",
        ExerciseSessionRecord.EXERCISE_TYPE_SCUBA_DIVING to "Scuba Diving",
        ExerciseSessionRecord.EXERCISE_TYPE_SKATING to "Skating",
        ExerciseSessionRecord.EXERCISE_TYPE_SKIING to "Skiing",
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWBOARDING to "Snowboarding",
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWSHOEING to "Snowshoeing",
        ExerciseSessionRecord.EXERCISE_TYPE_SOCCER to "Soccer",
        ExerciseSessionRecord.EXERCISE_TYPE_SOFTBALL to "Softball",
        ExerciseSessionRecord.EXERCISE_TYPE_SQUASH to "Squash",
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING to "Stair Climbing",
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING_MACHINE to "Stair Climbing Machine",
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING to "Strength Training",
        ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING to "Stretching",
        ExerciseSessionRecord.EXERCISE_TYPE_SURFING to "Surfing",
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER to "Swimming (Open Water)",
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL to "Swimming (Pool)",
        ExerciseSessionRecord.EXERCISE_TYPE_TABLE_TENNIS to "Table Tennis",
        ExerciseSessionRecord.EXERCISE_TYPE_TENNIS to "Tennis",
        ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL to "Volleyball",
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING to "Walking",
        ExerciseSessionRecord.EXERCISE_TYPE_WATER_POLO to "Water Polo",
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING to "Weightlifting",
        ExerciseSessionRecord.EXERCISE_TYPE_WHEELCHAIR to "Wheelchair",
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA to "Yoga",
    )

    private val reverseExerciseTypeMap = exerciseTypeMap.entries.associate { (k, v) -> v to k }
    fun toName(type: Int): String {
        return exerciseTypeMap[type] ?: "Unknown Exercise"
    }

    fun toId(name: String): Int? {
        return reverseExerciseTypeMap[name]
    }

    fun getSupportedTypes(): List<String> {
        return exerciseTypeMap.values.sorted()
    }
}