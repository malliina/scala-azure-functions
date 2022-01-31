package com.malliina.saf

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Zip:
  val log = AppLogger(getClass)

  def zipFolder(dir: Path): Path =
    val target = dir.getParent.resolve(s"${dir.getFileName.toString}.zip")
    val zos = new ZipOutputStream(new FileOutputStream(target.toFile))
    try Files.walkFileTree(
      dir,
      new SimpleFileVisitor[Path]():
        override def visitFile(
          file: Path,
          attributes: BasicFileAttributes
        ): FileVisitResult =
          // only copy files, no symbolic links
          if attributes.isSymbolicLink then FileVisitResult.CONTINUE
          else
            val fis = new FileInputStream(file.toFile)
            try
              val targetFile = dir.relativize(file)
              zos.putNextEntry(new ZipEntry(targetFile.toString.replace('\\', '/')))
              val buffer = new Array[Byte](1024)
              var len = 0
              while {
                len = fis.read(buffer)
                len > 0
              } do zos.write(buffer, 0, len)
              zos.closeEntry()
            finally fis.close()
            FileVisitResult.CONTINUE

        override def visitFileFailed(file: Path, exc: IOException) =
          FileVisitResult.CONTINUE
    )
    finally zos.close()
    log.info(s"Zipped $dir to $target.")
    target
