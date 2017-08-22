FROM openjdk:8

EXPOSE 18001

ENTRYPOINT ["java", "-jar", "/barrio/barrio.jar", \
            "--isUrl", \
            "-f", "https://media.githubusercontent.com/media/codiply/barrio-test-data/master/movielens-item-factors/rank-16.txt", \
            "-d", "16", \
            "-m", "cosine", \
            "--separator", "@~@"]