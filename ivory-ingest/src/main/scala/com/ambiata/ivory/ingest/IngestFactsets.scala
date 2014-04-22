package com.ambiata.ivory.ingest

import com.ambiata.mundane.control._
import com.ambiata.mundane.io._
import com.ambiata.ivory.core._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress._
import org.joda.time.DateTimeZone
import com.nicta.scoobi.Scoobi._

import scalaz._, Scalaz._, effect._, Effect._


sealed trait IngestFactsets {
  def ingest(dictionary: Dictionary, namespace: String, factsetId: String, input: FilePath, timezone: DateTimeZone, codec: Option[CompressionCodec]): ResultT[IO, Unit]
}

case class S3EavtIngestFactsets(conf: ScoobiConfiguration, repository: S3Repository, transform: String => String) {
  def ingest(dictionary: Dictionary, namespace: String, factsetId: String, input: FilePath, timezone: DateTimeZone, codec: Option[CompressionCodec]): ResultT[IO, Unit] =
    EavtTextImporter.onS3(repository, dictionary, factsetId, namespace, input, timezone, codec, transform).runScoobiAwsT(conf)
}

case class MapReduceEavtIngestFactsets(conf: ScoobiConfiguration, repository: HdfsRepository, transform: String => String) {
  def ingest(dictionary: Dictionary, namespace: String, factsetId: String, input: FilePath, timezone: DateTimeZone, codec: Option[CompressionCodec]): ResultT[IO, Unit] =
    EavtTextImporter.onHdfs(repository, dictionary, factsetId, namespace, new Path(input.path), repository.errorsPath, timezone, codec, transform).run(conf)
}

case class HdfsEavtIngestFactsets(conf: Configuration, repository: HdfsRepository, transform: String => String) {
  def ingest(dictionary: Dictionary, namespace: String, factsetId: String, input: FilePath, timezone: DateTimeZone, codec: Option[CompressionCodec]): ResultT[IO, Unit] =
    EavtTextImporter.onHdfsDirect(conf, repository, dictionary, factsetId, namespace, new Path(input.path), repository.errorsPath, timezone, codec, transform)
}