package com.katiba.parser

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * Constitution Parser Main Entry Point
 *
 * Usage: Run this tool to parse the Constitution of Kenya PDF and generate YAML output.
 *
 * The parser will:
 * 1. Read the PDF from docs/Constitution_of_Kenya.pdf
 * 2. Extract and structure all chapters, articles, and clauses
 * 3. Output YAML to composeApp/src/commonMain/resources/constitution.yaml
 */
fun main(args: Array<String>) {
    println("=".repeat(60))
    println("Constitution of Kenya PDF Parser")
    println("=".repeat(60))

    // Determine paths
    val projectRoot = File(".").absoluteFile.parentFile
    val pdfPath = args.getOrNull(0) ?: "docs/Constitution_of_Kenya.pdf"
    val outputPath = args.getOrNull(1) ?: "composeApp/src/commonMain/composeResources/files/constitution.yaml"

    val pdfFile = File(projectRoot, pdfPath)
    val outputFile = File(projectRoot, outputPath)

    println("PDF Input: ${pdfFile.absolutePath}")
    println("YAML Output: ${outputFile.absolutePath}")
    println()

    if (!pdfFile.exists()) {
        System.err.println("ERROR: PDF file not found at ${pdfFile.absolutePath}")
        System.exit(1)
    }

    try {
        // Parse the PDF
        println("Parsing PDF...")
        val parser = ConstitutionPdfParser(pdfFile)

        // First, let's extract and save the raw text for inspection
        val rawText = parser.extractText()
        val textOutputFile = File(projectRoot, "docs/constitution_raw_text.txt")
        textOutputFile.writeText(rawText)
        println("Raw text saved to: ${textOutputFile.absolutePath}")
        println("Raw text length: ${rawText.length} characters")
        println()

        // Parse into structured data
        val constitution = parser.parse()

        // Print summary
        println("Parsing complete!")
        println("-".repeat(40))
        println("Preamble: ${constitution.preamble.take(100)}...")
        println("Chapters found: ${constitution.chapters.size}")
        constitution.chapters.forEach { chapter ->
            println("  Chapter ${chapter.number}: ${chapter.title}")
            println("    Articles: ${chapter.articles.size}")
            chapter.articles.take(3).forEach { article ->
                println("      Article ${article.number}: ${article.title}")
                println("        Clauses: ${article.clauses.size}")
            }
            if (chapter.articles.size > 3) {
                println("      ... and ${chapter.articles.size - 3} more articles")
            }
        }
        println()

        // Serialize to YAML
        println("Generating YAML...")
        val yaml = Yaml(configuration = YamlConfiguration(
            encodeDefaults = true,
            strictMode = false
        ))

        val yamlContent = yaml.encodeToString(constitution)

        // Ensure output directory exists
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(yamlContent)

        println("YAML saved to: ${outputFile.absolutePath}")
        println("YAML size: ${outputFile.length()} bytes")
        println()
        println("=".repeat(60))
        println("SUCCESS! Constitution parsed and saved.")
        println("=".repeat(60))

    } catch (e: Exception) {
        System.err.println("ERROR: Failed to parse PDF")
        e.printStackTrace()
        System.exit(1)
    }
}

