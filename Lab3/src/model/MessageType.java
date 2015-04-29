package model;

public enum MessageType {
	CAPTURE, // Candidate -> Ordinary
	CAPTURED, // Ordinary -> Candidate
	KILLED, // Candidate -> Ordinary
	RECAPTURED // Ordinary -> Candidate
}
