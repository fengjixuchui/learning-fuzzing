#pragma once

#include "types.hpp"

#include <iosfwd>
#include <string>
#include <unordered_map>
#include <utility>

struct mealy;
struct translation;

/// \brief reads a mealy machine from plain txt file as provided by A. T. Endo
/// States, inputs and outputs in these files are already integral, so no need for translation
mealy read_mealy_from_txt(std::istream & in, bool check = true);
mealy read_mealy_from_txt(std::string const & filename, bool check = true);

/// \brief reads a mealy machine from dot files as generated by learnlib
/// Here we need a translation, which is extended during parsing
mealy read_mealy_from_dot(std::istream & in, translation & t, bool check = true);
mealy read_mealy_from_dot(std::string const & filename, translation & t, bool check = true);

/// \brief reads a mealy machine from dot files as generated by learnlib
/// Here the translation starts out empty and is returned in the end
std::pair<mealy, translation> read_mealy_from_dot(std::istream & in, bool check = true);
std::pair<mealy, translation> read_mealy_from_dot(std::string const & filename, bool check = true);


/// \brief For non-integral formats we use a translation to integers
struct translation {
	std::unordered_map<std::string, input> input_indices;
	input max_input = 0;

	std::unordered_map<std::string, output> output_indices;
	output max_output = 0;
};

/// \brief inverts the input_indices and output_indices maps
std::vector<std::string> create_reverse_map(std::unordered_map<std::string, input> const & indices);
std::vector<std::string> create_reverse_map(std::unordered_map<std::string, output> const & indices);

/// \brief defines trivial translation (the string represent integers directly)
translation create_translation_for_mealy(mealy const & m);
